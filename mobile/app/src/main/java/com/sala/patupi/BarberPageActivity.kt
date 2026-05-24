package com.sala.patupi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

data class PatupiServiceItem(val id: Int, val name: String)

@SuppressLint("SetTextI18n", "ClickableViewAccessibility")
class BarberPageActivity : AppCompatActivity() {

    private lateinit var queueContainer: LinearLayout
    private lateinit var tvWelcome: TextView
    private lateinit var tvQueueCount: TextView

    // --- API Configuration Base Endpoints ---
    private val apiBase = "http://192.168.1.2:8080/api/appointments"
    private val servicesApiUrl = "http://192.168.1.2:8080/api/services"
    private val barberProfileApiUrl = "http://192.168.1.2:8080/api/barbers"

    // Master Cache Dictionary for mapping service_id -> service Name string
    private val servicesCache = HashMap<Int, PatupiServiceItem>()

    // --- AUTOMATIC RUNTIME REFRESH MODULE CONTROLS ---
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshIntervalMs: Long = 5000L
    private var lastObservedQueueLength = -1

    private val autoRefreshTask = object : Runnable {
        override fun run() {
            fetchBarberQueueSilently()
            refreshHandler.postDelayed(this, refreshIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barberpage)

        queueContainer = findViewById(R.id.barberQueueContainer)
        tvWelcome = findViewById(R.id.tvBarberWelcome)
        tvQueueCount = findViewById(R.id.tvQueueCount)

        val userObj = SessionManager.currentUserJson
        val name = userObj?.optString("fullName", "Barber")
        tvWelcome.text = "Logged in as: $name"

        findViewById<TextView>(R.id.btnBarberLogout).setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Fetch master service index registry from backend server first
        fetchServicesRegistryMap()
    }

    override fun onResume() {
        super.onResume()
        lastObservedQueueLength = -1
        fetchBarberQueue()
        refreshHandler.postDelayed(autoRefreshTask, refreshIntervalMs)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(autoRefreshTask)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to exit the barber workspace session?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss()

                // 🌟 Set status to Unavailable automatically when logging out
                val userObj = SessionManager.currentUserJson
                val barberId = userObj?.optInt("userId", userObj.optInt("id", -1)) ?: -1
                updateBarberAvailabilityStatus(barberId, "Unavailable")

                SessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                finish()
            }
            .show()
    }

    // Downloads complete shop inventory profiles to match ID integers to real text strings
    private fun fetchServicesRegistryMap() {
        val request = JsonArrayRequest(Request.Method.GET, servicesApiUrl, null,
            { response ->
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val id = obj.optInt("service_id", obj.optInt("id"))
                        val name = obj.optString("name", "Hair Treatment")
                        servicesCache[id] = PatupiServiceItem(id, name)
                    }

                    // 🌟 FIXED: Set status to "Available" (matches React admin dashboard expectations)
                    val userObj = SessionManager.currentUserJson
                    val barberId = userObj?.optInt("userId", userObj.optInt("id", -1)) ?: -1
                    updateBarberAvailabilityStatus(barberId, "Available")

                    // Re-render the visual list with freshly downloaded names mapped
                    fetchBarberQueueSilently()
                } catch (e: Exception) {
                    Log.e("PATUPI_SERVICES", "Failed parsing menu details: ${e.message}")
                }
            },
            { error ->
                Log.e("PATUPI_SERVICES", "Could not reach shop services endpoints: ${error.message}")
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    // Handles live presence updates for Busy, Available, or Unavailable profiles
    private fun updateBarberAvailabilityStatus(barberId: Int, newStatus: String) {
        if (barberId == -1) return
        val url = "$barberProfileApiUrl/$barberId/status"
        val payload = JSONObject().apply { put("status", newStatus) }

        val req = JsonObjectRequest(Request.Method.PUT, url, payload,
            { _ -> Log.d("PATUPI_STATUS", "Barber profile status updated to: $newStatus") },
            { err -> Log.e("PATUPI_STATUS", "Failed updating visibility presence: ${err.message}") }
        )
        Volley.newRequestQueue(this).add(req)
    }

    private fun fetchBarberQueue() {
        processQueueSync(clearViewsOnHit = true)
    }

    private fun fetchBarberQueueSilently() {
        processQueueSync(clearViewsOnHit = false)
    }

    private fun processQueueSync(clearViewsOnHit: Boolean) {
        val userObj = SessionManager.currentUserJson ?: return
        val barberId = userObj.optInt("userId", userObj.optInt("id", -1))
        val url = "$apiBase/barber/$barberId"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                tvQueueCount.text = response.length().toString()

                // Scan active loops to determine status dynamically based on current appointments
                var hasInProgressTask = false
                for (x in 0 until response.length()) {
                    if (response.getJSONObject(x).optString("status") == "IN_PROGRESS") {
                        hasInProgressTask = true
                        break
                    }
                }

                // 🌟 FIXED: Switches between "Busy" or "Available" (matches React)
                updateBarberAvailabilityStatus(barberId, if (hasInProgressTask) "Busy" else "Available")

                if (!clearViewsOnHit && response.length() == lastObservedQueueLength) {
                    return@JsonArrayRequest
                }

                lastObservedQueueLength = response.length()
                queueContainer.removeAllViews()

                if (response.length() == 0) {
                    val tvEmpty = TextView(this).apply {
                        text = "No active appointments in your queue."
                        // 🌟 FIXED: Use standard Color.parseColor to avoid crash
                        setTextColor(Color.parseColor("#5A5A65"))
                        textSize = 15f
                        setPadding(0, 80, 0, 0)
                        gravity = Gravity.CENTER
                        alpha = 0f
                    }
                    queueContainer.addView(tvEmpty)
                    tvEmpty.animate().alpha(1f).setDuration(400).start()
                    return@JsonArrayRequest
                }

                for (i in 0 until response.length()) {
                    val appt = response.getJSONObject(i)
                    addQueueRow(appt, index = i)
                }
            },
            { error ->
                Log.e("PATUPI_BARBER", "Queue update dropped: ${error.message}")
                if (clearViewsOnHit) {
                    Toast.makeText(this, "Failed to load operations data.", Toast.LENGTH_SHORT).show()
                }
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun addQueueRow(appt: JSONObject, index: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_barber_queue, queueContainer, false)
        val card = itemView.findViewById<MaterialCardView>(R.id.cardRoot)
        val tvTime = itemView.findViewById<TextView>(R.id.tvCardTime)
        val tvStatusPill = itemView.findViewById<TextView>(R.id.tvCardStatusPill)
        val tvCustomer = itemView.findViewById<TextView>(R.id.tvCardCustomer)
        val tvService = itemView.findViewById<TextView>(R.id.tvCardService)
        val tvPrice = itemView.findViewById<TextView>(R.id.tvCardPrice)

        val rawDate = appt.optString("scheduledAt")
        tvTime.text = formatIsoDate(rawDate).uppercase()
        tvCustomer.text = appt.optString("customerName", "Guest User")

        // 1. Try to extract Service ID from EVERY possible variation at root level
        var targetServiceId = appt.optInt("serviceId", -1)
        if (targetServiceId == -1) targetServiceId = appt.optInt("service_id", -1)
        if (targetServiceId == -1) targetServiceId = appt.optInt("id", -1) // Fallback check

        // 2. If it's a nested object structure, look deep inside {"service": { ... }}
        val nestedServiceObj = appt.optJSONObject("service")
        if (nestedServiceObj != null) {
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("id", -1)
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("service_id", -1)
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("serviceId", -1)
        }

        // 3. RESOLVE ACTUAL SERVICE NAME OR CRASH-PROOF FALLBACKS
        if (targetServiceId != -1 && servicesCache.containsKey(targetServiceId)) {
            // Priority A: Found in Cache map via ID match
            tvService.text = servicesCache[targetServiceId]?.name
        } else {
            // Priority B: Check if backend included literal name strings directly in root object
            val rootNameFallback = appt.optString("serviceName", appt.optString("service_name", ""))

            // Priority C: Check if backend included literal name strings inside the nested object
            val nestedNameFallback = nestedServiceObj?.optString("name", nestedServiceObj.optString("serviceName", "")) ?: ""

            if (rootNameFallback.isNotEmpty()) {
                tvService.text = rootNameFallback
            } else if (nestedNameFallback.isNotEmpty()) {
                tvService.text = nestedNameFallback
            } else {
                // Priority D: Ultimate failsafe so you don't get "Special Styling Session" silently
                tvService.text = "Service (ID: $targetServiceId)"
            }
        }

        val totalAmountVal = appt.optDouble("totalAmount", appt.optDouble("total_amount", 0.0))
        tvPrice.text = "₱${totalAmountVal.toInt()}"
        tvPrice.visibility = View.VISIBLE

        val statusStr = appt.optString("status").uppercase(Locale.getDefault())
        tvStatusPill.text = statusStr

        if (statusStr == "IN_PROGRESS") {
            tvStatusPill.setBackgroundColor(Color.parseColor("#1A2A3A"))
            tvStatusPill.setTextColor(Color.parseColor("#2196F3"))
        } else {
            tvStatusPill.setBackgroundColor(Color.parseColor("#241E12"))
            tvStatusPill.setTextColor(Color.parseColor("#D4AF37"))
        }

        card.alpha = 0f
        card.translationY = 40f

        card.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).setInterpolator(OvershootInterpolator()).start()
                    if (event.action == MotionEvent.ACTION_UP) {
                        showBarberActionModal(appt)
                    }
                }
            }
            true
        }

        queueContainer.addView(itemView)

        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350)
            .setStartDelay(index * 60L)
            .setInterpolator(OvershootInterpolator(0.7f))
            .start()
    }

    private fun showBarberActionModal(appt: JSONObject) {
        val dialogView = layoutInflater.inflate(R.layout.modal_barber_action, null)

        val isAlreadyInProgress = appt.optString("status") == "IN_PROGRESS"
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(!isAlreadyInProgress)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(android.R.style.Animation_Dialog)

        val tvName = dialogView.findViewById<TextView>(R.id.modalBarberCustomerName)
        val tvDetails = dialogView.findViewById<TextView>(R.id.modalBarberDetails)
        val btnStart = dialogView.findViewById<MaterialButton>(R.id.btnStartAppointment)
        val layoutSwipe = dialogView.findViewById<RelativeLayout>(R.id.layoutSwipeTrack)
        val swipeThumb = dialogView.findViewById<View>(R.id.swipeThumb)
        val swipeFiller = dialogView.findViewById<View>(R.id.swipeProgressFiller)
        val tvThumbText = dialogView.findViewById<TextView>(R.id.tvSwipeThumbText)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnDismissBarberModal)

        val appointmentIdRaw = appt.optString("id", appt.optString("appointmentId", "0"))
        val targetAppointmentId = appointmentIdRaw.toLongOrNull() ?: appt.optLong("id", 0L)

        // 1. Try to extract Service ID from EVERY possible variation at root level inside modal
        var targetServiceId = appt.optInt("serviceId", -1)
        if (targetServiceId == -1) targetServiceId = appt.optInt("service_id", -1)

        // 2. Nested check inside modal
        val nestedServiceObj = appt.optJSONObject("service")
        if (nestedServiceObj != null) {
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("id", -1)
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("service_id", -1)
            if (targetServiceId == -1) targetServiceId = nestedServiceObj.optInt("serviceId", -1)
        }

        // 3. RESOLVE ACTUAL SERVICE NAME FOR MODAL
        val resolvedServiceName = if (targetServiceId != -1 && servicesCache.containsKey(targetServiceId)) {
            servicesCache[targetServiceId]?.name
        } else {
            val rootNameFallback = appt.optString("serviceName", appt.optString("service_name", ""))
            val nestedNameFallback = nestedServiceObj?.optString("name", nestedServiceObj.optString("serviceName", "")) ?: ""

            if (rootNameFallback.isNotEmpty()) {
                rootNameFallback
            } else if (nestedNameFallback.isNotEmpty()) {
                nestedNameFallback
            } else {
                "Service (ID: $targetServiceId)"
            }
        }

        val totalAmountVal = appt.optDouble("totalAmount", appt.optDouble("total_amount", 0.0))
        val notes = appt.optString("description", "No extra requests provided.")
        val timeLabel = formatIsoDate(appt.optString("scheduledAt"))

        val currentBarberUserObj = SessionManager.currentUserJson
        val currentBarberId = currentBarberUserObj?.optInt("userId", currentBarberUserObj.optInt("id", -1)) ?: -1

        tvName.text = appt.optString("customerName", "Guest User")
        tvDetails.text = "Service: $resolvedServiceName\nPrice: ₱${totalAmountVal.toInt()}\nTime Slot: $timeLabel\n\nNotes:\n$notes"

        if (isAlreadyInProgress) {
            btnStart.visibility = View.GONE
            layoutSwipe.visibility = View.VISIBLE
            btnClose.visibility = View.GONE
        } else {
            btnStart.visibility = View.VISIBLE
            layoutSwipe.visibility = View.GONE
            btnClose.visibility = View.VISIBLE
        }

        btnStart.setOnClickListener {
            updateStatusBackend(targetAppointmentId, "IN_PROGRESS") {
                btnStart.visibility = View.GONE
                layoutSwipe.visibility = View.VISIBLE
                btnClose.visibility = View.GONE
                dialog.setCancelable(false)
                updateBarberAvailabilityStatus(currentBarberId, "Busy")
                fetchBarberQueue()
            }
        }

        var touchDeltaX = 0f
        var isCompleted = false

        swipeThumb.setOnTouchListener { v, event ->
            if (isCompleted) return@setOnTouchListener false

            val maxSwipe = (layoutSwipe.width - v.width).toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDeltaX = v.x - event.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (maxSwipe <= 0) return@setOnTouchListener true

                    var newX = event.rawX + touchDeltaX
                    if (newX < 0f) newX = 0f
                    if (newX > maxSwipe) newX = maxSwipe

                    v.x = newX

                    val lp = swipeFiller.layoutParams
                    lp.width = (newX + (v.width / 2)).toInt()
                    swipeFiller.layoutParams = lp

                    tvThumbText.x = newX
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (maxSwipe <= 0) return@setOnTouchListener true

                    val progressPercentage = v.x / maxSwipe

                    if (progressPercentage >= 0.85f && event.action == MotionEvent.ACTION_UP) {
                        isCompleted = true
                        swipeThumb.setOnTouchListener(null)
                        updateStatusBackend(targetAppointmentId, "COMPLETED") {
                            updateBarberAvailabilityStatus(currentBarberId, "Available")
                            dialog.dismiss()
                            fetchBarberQueue()
                        }
                    } else {
                        v.animate().x(0f).setDuration(250).setInterpolator(OvershootInterpolator()).start()
                        tvThumbText.animate().x(0f).setDuration(250).setInterpolator(OvershootInterpolator()).start()

                        val anim = object : android.view.animation.Animation() {
                            override fun applyTransformation(interpolatedTime: Float, t: android.view.animation.Transformation?) {
                                val lp = swipeFiller.layoutParams
                                lp.width = (lp.width * (1 - interpolatedTime)).toInt()
                                swipeFiller.layoutParams = lp
                            }
                        }
                        anim.duration = 250
                        swipeFiller.startAnimation(anim)
                    }
                    true
                }
                else -> false
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updateStatusBackend(id: Long, status: String, onSuccess: () -> Unit) {
        val url = "$apiBase/$id/status"
        val payload = JSONObject().apply { put("status", status) }

        val req = JsonObjectRequest(Request.Method.PUT, url, payload,
            { _ ->
                Toast.makeText(this, "Session state shifted: $status", Toast.LENGTH_SHORT).show()
                onSuccess()
            },
            { error ->
                Log.e("PATUPI_STATUS", "Status mutation parsing drop: ${error.message}")
                Toast.makeText(this, "Action update rejected by host instance.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(req)
    }

    private fun formatIsoDate(iso: String): String {
        return try {
            val cleanIso = if (iso.contains(".")) iso.substringBefore(".") else iso
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            formatter.format(parser.parse(cleanIso)!!)
        } catch (_: Exception) { iso }
    }
}