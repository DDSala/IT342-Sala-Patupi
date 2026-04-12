package com.sala.patupi

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

    private var currentActiveId: Int? = null
    private var activeAppointmentJson: JSONObject? = null
    private val apiBase = "http://192.168.1.9:8080/api/appointments"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        setupListeners()
        fetchWeather()
    }

    override fun onResume() {
        super.onResume()
        fetchData()
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
        val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("user", null) ?: return
        val userObj = JSONObject(userJson)
        val userId = userObj.optInt("userId", userObj.optInt("id", 1))

        val request = JsonArrayRequest(Request.Method.GET, "$apiBase/customer/$userId", null,
            { response ->
                historyContainer.removeAllViews()
                var activeFound = false

                val appointments = mutableListOf<JSONObject>()
                for (i in 0 until response.length()) appointments.add(response.getJSONObject(i))
                appointments.sortByDescending { it.optString("scheduledAt") }

                appointments.forEachIndexed { idx, appt ->
                    val status = appt.getString("status").uppercase(Locale.getDefault())
                    val desc = appt.optString("serviceName", "Grooming")
                    val date = appt.optString("scheduledAt")

                    if (!activeFound && status in listOf("CONFIRMED", "PENDING", "DRAFT")) {
                        updateActiveUI(appt, status, desc, date)
                        activeFound = true
                    } else if (status in listOf("COMPLETED", "CANCELLED")) {
                        addHistoryRow(date, desc, status, idx)
                    }
                }
                if (!activeFound) resetActiveUI()
            },
            { Log.e("PATUPI", "Fetch Failed") }
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
        if (status == "CONFIRMED") {
            tvActiveStatus.setBackgroundResource(R.drawable.status_pill_confirmed)
            tvActiveStatus.setTextColor("#4CAF50".toColorInt())
        } else {
            tvActiveStatus.setBackgroundResource(R.drawable.status_pill_pending)
            tvActiveStatus.setTextColor(Color.BLACK)
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
        // 1. Get the userId from SharedPrefs (just like Web uses sessionStorage)
        val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("user", null) ?: return
        val userObj = JSONObject(userJson)
        val userId = userObj.optInt("userId", userObj.optInt("id", 1))

        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                // 2. Add the ?customerId= parameter to match the Web implementation
                val url = "$apiBase/$id/cancel?customerId=$userId"

                val req = JsonObjectRequest(Request.Method.PUT, url, null,
                    { _ ->
                        Toast.makeText(this, "Appointment Cancelled", Toast.LENGTH_SHORT).show()
                        fetchData() // Refresh UI
                    },
                    { error ->
                        // Log the full error to see if it's a 400 or 404
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
        row.addView(createCell("—", 0.8f, Gravity.CENTER))
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
}