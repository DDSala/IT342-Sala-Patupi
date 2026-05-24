package com.sala.patupi

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTemp: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvActiveService: TextView
    private lateinit var tvActiveDate: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var tvActiveStatus: TextView
    private lateinit var tvTapHint: TextView
    private lateinit var layoutActiveDetails: LinearLayout
    private lateinit var layoutActiveActions: LinearLayout
    private lateinit var historyContainer: LinearLayout
    private lateinit var cardActiveTicket: MaterialCardView
    private lateinit var btnCancelTicket: MaterialButton
    private lateinit var btnBookNow: MaterialButton

    private var currentActiveId: Int? = null
    private var activeAppointmentJson: JSONObject? = null
    private var hasActiveTicket: Boolean = false
    private val apiBase = "http://192.168.1.2:8080/api/appointments"

    // --- AUTOMATIC RUNTIME REFRESH MODULE CONTROLS ---
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshIntervalMs: Long = 5000L // 5 Seconds background sync cadence loop

    private val autoRefreshTask = object : Runnable {
        override fun run() {
            Log.d("PATUPI_SYNC", "Executing background operation queue sync check...")
            fetchDataSilently()
            refreshHandler.postDelayed(this, refreshIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        setupListeners()
        fetchWeather()
    }

    override fun onResume() {
        super.onResume()
        // Force an initial synchronous load on screen entrance
        fetchData()
        // Begin the background execution loop pipeline immediately
        refreshHandler.postDelayed(autoRefreshTask, refreshIntervalMs)
    }

    override fun onPause() {
        super.onPause()
        // CRITICAL FIX: Kill active thread hooks to completely prevent ghost leaks when exiting dashboard layout scopes
        refreshHandler.removeCallbacks(autoRefreshTask)
        Log.d("PATUPI_SYNC", "Suspended background execution tasks.")
    }

    private fun initializeViews() {
        tvTemp = findViewById(R.id.tvTemp)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvActiveService = findViewById(R.id.tvActiveService)
        tvActiveDate = findViewById(R.id.tvActiveDate)
        tvActiveTime = findViewById(R.id.tvActiveTime)
        tvActiveStatus = findViewById(R.id.tvActiveStatus)
        tvTapHint = findViewById(R.id.tvTapHint)
        layoutActiveDetails = findViewById(R.id.layoutActiveDetails)
        layoutActiveActions = findViewById(R.id.layoutActiveActions)
        historyContainer = findViewById(R.id.historyContainer)
        cardActiveTicket = findViewById(R.id.cardActiveTicket)
        btnCancelTicket = findViewById(R.id.btnCancelTicket)
        btnBookNow = findViewById(R.id.btnBookNow)
    }

    private fun setupListeners() {
        cardActiveTicket.setOnClickListener {
            activeAppointmentJson?.let { showDetailsModal(it) }
        }

        btnCancelTicket.setOnClickListener {
            currentActiveId?.let { handleCancel(it) }
        }

        findViewById<MaterialCardView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnBookNow.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }
    }

    private fun setBookingButtonState(isLocked: Boolean) {
        if (isLocked) {
            btnBookNow.isEnabled = false
            btnBookNow.text = "Booking Locked"
            btnBookNow.backgroundTintList = android.content.res.ColorStateList.valueOf("#555555".toColorInt())
            btnBookNow.setTextColor("#888888".toColorInt())
        } else {
            btnBookNow.isEnabled = true
            btnBookNow.text = "Book Now"
            btnBookNow.backgroundTintList = android.content.res.ColorStateList.valueOf("#D4AF37".toColorInt())
            btnBookNow.setTextColor(Color.BLACK)
        }
    }

    private fun fetchWeather() {
        val lat = 10.3157
        val lon = 123.8854
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val current = response.getJSONObject("current")
                    val temp = current.getDouble("temperature_2m")
                    tvTemp.text = String.format(Locale.getDefault(), "%d°C", temp.roundToInt())
                    ivWeatherIcon.setImageResource(R.drawable.ic_cloudy)
                } catch (_: Exception) { }
            },
            { tvTemp.text = "--°C" }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchData() {
        processDashboardSync(showErrorsOnUi = true)
    }

    private fun fetchDataSilently() {
        processDashboardSync(showErrorsOnUi = false)
    }

    private fun processDashboardSync(showErrorsOnUi: Boolean) {
        val userObj = SessionManager.currentUserJson
        if (userObj == null) {
            Log.e("PATUPI", "Fetch Failed: No user session found in memory.")
            if (showErrorsOnUi) redirectToLogin()
            return
        }

        val userId = userObj.optInt("userId", userObj.optInt("id", 1))
        val url = "$apiBase/customer/$userId"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                var activeFound = false
                val appointments = mutableListOf<JSONObject>()

                for (i in 0 until response.length()) {
                    appointments.add(response.getJSONObject(i))
                }
                appointments.sortByDescending { it.optString("scheduledAt") }

                val historicalItems = appointments.filter {
                    it.getString("status").uppercase(Locale.getDefault()) in listOf("COMPLETED", "CANCELLED")
                }

                if (historyContainer.childCount != historicalItems.size) {
                    historyContainer.removeAllViews()
                    historicalItems.forEachIndexed { idx, appt ->
                        val desc = appt.optString("serviceName", "Grooming")
                        val date = appt.optString("scheduledAt")
                        val status = appt.getString("status").uppercase(Locale.getDefault())
                        addHistoryRow(date, desc, status, idx)
                    }
                }

                appointments.forEach { appt ->
                    val status = appt.getString("status").uppercase(Locale.getDefault())
                    val desc = appt.optString("serviceName", "Grooming")
                    val date = appt.optString("scheduledAt")

                    if (!activeFound && status in listOf("CONFIRMED", "PENDING", "DRAFT", "IN_PROGRESS")) {
                        updateActiveUI(appt, status, desc, date)
                        activeFound = true
                    }
                }

                hasActiveTicket = activeFound
                setBookingButtonState(hasActiveTicket)

                if (!activeFound) {
                    resetActiveUI()
                }
            },
            { error ->
                Log.e("PATUPI", "Sync Lifecycle Pipeline Hit Error: ${error.message}")
                if (showErrorsOnUi) {
                    Toast.makeText(this, "Network synchronization dropped.", Toast.LENGTH_SHORT).show()
                }
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateActiveUI(appt: JSONObject, status: String, desc: String, rawDate: String) {
        activeAppointmentJson = appt
        currentActiveId = appt.getInt("appointmentId")

        tvActiveService.text = desc
        tvActiveDate.text = formatDate(rawDate, "MM/dd/yy")
        tvActiveTime.text = formatDate(rawDate, "hh:mm a")

        layoutActiveDetails.visibility = View.VISIBLE
        layoutActiveActions.visibility = View.VISIBLE
        tvTapHint.visibility = View.VISIBLE

        tvActiveStatus.text = status
        when (status) {
            "CONFIRMED" -> {
                tvActiveStatus.setBackgroundResource(R.drawable.status_pill_confirmed)
                tvActiveStatus.setTextColor("#4CAF50".toColorInt())
            }
            "IN_PROGRESS" -> {
                tvActiveStatus.setBackgroundResource(R.drawable.status_pill_confirmed)
                tvActiveStatus.setTextColor("#2196F3".toColorInt())
            }
            else -> {
                tvActiveStatus.setBackgroundResource(R.drawable.status_pill_pending)
                tvActiveStatus.setTextColor(Color.BLACK)
            }
        }
    }

    private fun resetActiveUI() {
        activeAppointmentJson = null
        currentActiveId = null
        tvActiveService.text = "No active sessions"
        layoutActiveDetails.visibility = View.GONE
        layoutActiveActions.visibility = View.GONE
        tvTapHint.visibility = View.GONE
    }

    private fun showDetailsModal(appt: JSONObject) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.modal_appointment_details, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val id = appt.optInt("appointmentId", 0)
            val price = appt.optDouble("totalAmount", 0.0)

            dialogView.findViewById<TextView>(R.id.modalServiceName)?.text = appt.optString("serviceName")
            dialogView.findViewById<TextView>(R.id.modalRefCode)?.text = String.format(Locale.getDefault(), "PT-%04d", id)
            dialogView.findViewById<TextView>(R.id.modalDate)?.text = formatDate(appt.optString("scheduledAt"), "MMMM dd, yyyy")
            dialogView.findViewById<TextView>(R.id.modalTime)?.text = formatDate(appt.optString("scheduledAt"), "hh:mm a")
            dialogView.findViewById<TextView>(R.id.modalPrice)?.text = String.format(Locale.getDefault(), "PHP %.2f", price)

            dialogView.findViewById<MaterialButton>(R.id.btnModalClose)?.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (_: Exception) { }
    }

    private fun handleCancel(id: Int) {
        val userObj = SessionManager.currentUserJson ?: return
        val userId = userObj.optInt("userId", userObj.optInt("id", 1))

        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { dialog, _ ->
                val url = "$apiBase/$id/cancel?customerId=$userId"

                val req = JsonObjectRequest(Request.Method.PUT, url, null,
                    { _ ->
                        Toast.makeText(this, "Appointment Cancelled", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchData()
                    },
                    { error ->
                        val response = error.networkResponse
                        Log.e("PATUPI_CANCEL", "Error Code: ${response?.statusCode} | Message: ${error.message}")
                        Toast.makeText(this, "Failed to cancel. Check server.", Toast.LENGTH_SHORT).show()
                    }
                )
                Volley.newRequestQueue(this).add(req)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun addHistoryRow(rawDate: String, desc: String, status: String, index: Int) {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 48, 8, 48)
        }
        row.addView(createCell(formatDate(rawDate, "MM/dd/yy"), 1.2f))
        row.addView(createCell(formatDate(rawDate, "hh:mm a"), 1.2f))
        row.addView(createCell(desc, 2f))
        // REMOVED EXTRA BLANK CELL RATING MISALIGNMENT TO MATCH xml WEIGHT CONSTANTS
        row.addView(createCell(status, 1f, Gravity.END).apply {
            setTextColor(if (status == "CANCELLED") "#FF5252".toColorInt() else "#d4af37".toColorInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        anim.startOffset = (index * 50).toLong()
        row.startAnimation(anim)
        historyContainer.addView(row)
    }

    private fun createCell(text: String, weight: Float, grav: Int = Gravity.START): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -2, weight)
            this.text = text
            this.gravity = grav
            this.setTextColor(Color.WHITE)
            this.textSize = 11f
        }
    }

    private fun formatDate(iso: String, pattern: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.format(parser.parse(iso)!!)
        } catch (_: Exception) { "—" }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}