package com.sala.patupi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var tvStepNumber: TextView
    private lateinit var tvStepTitle: TextView
    private lateinit var btnWizardNext: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var progressBarFill: View
    private lateinit var containerServices: LinearLayout

    private lateinit var etBookingDate: EditText
    private lateinit var etBookingTime: EditText

    private lateinit var summaryService: TextView
    private lateinit var summaryDateTime: TextView
    private lateinit var summaryPrice: TextView

    // Booking Data States
    private var selectedServiceId: Int = -1
    private var selectedServiceName: String = ""
    private var selectedServicePrice: Double = 0.0

    private val servicesUrl = "http://192.168.1.9:8080/api/services"
    private val submitBookingUrl = "http://192.168.1.9:8080/api/appointments"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        initializeViews()
        setupBackPressedDispatcher()
        fetchServicesFromDatabase()
    }

    private fun initializeViews() {
        viewFlipper = findViewById(R.id.viewFlipper)
        tvStepNumber = findViewById(R.id.tvStepNumber)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        btnWizardNext = findViewById(R.id.btnWizardNext)
        btnBack = findViewById(R.id.btnBack)
        progressBarFill = findViewById(R.id.progressBarFill)
        containerServices = findViewById(R.id.containerServices)

        etBookingDate = findViewById(R.id.etBookingDate)
        etBookingTime = findViewById(R.id.etBookingTime)

        summaryService = findViewById(R.id.summaryService)
        summaryDateTime = findViewById(R.id.summaryDateTime)
        summaryPrice = findViewById(R.id.summaryPrice)

        btnBack.setOnClickListener { handleWizardBack() }
        btnWizardNext.setOnClickListener { handleWizardNext() }
    }

    // Resolves the onBackPressed deprecation completely using modern Android standards
    private fun setupBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewFlipper.displayedChild == 0) {
                    isEnabled = false // Disable callback to let system handle normal activity back exit
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    handleWizardBack()
                }
            }
        })
    }

    private fun fetchServicesFromDatabase() {
        val request = JsonArrayRequest(Request.Method.GET, servicesUrl, null,
            { response ->
                containerServices.removeAllViews()
                for (i in 0 until response.length()) {
                    val serviceObj = response.getJSONObject(i)
                    createServiceCardRow(serviceObj)
                }
            },
            { error ->
                Log.e("PATUPI_BOOKING", "Failed to retrieve services: ${error.message}")
                Toast.makeText(this, "Error fetching salon services menu.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun createServiceCardRow(service: JSONObject) {
        val id = service.getInt("id")
        val name = service.getString("name")
        val price = service.getDouble("price")
        val duration = service.getInt("duration")

        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 16) }
            radius = 16f
            setCardBackgroundColor("#1E1E1E".toColorInt()) // Fixed Color.parseColor warning
            strokeWidth = 2
            strokeColor = "#33FFFFFF".toColorInt()
            isClickable = true
            isFocusable = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
        }

        val tvName = TextView(this).apply {
            text = name
            setTextColor("#FFFFFF".toColorInt()) // Fixed textColor error & format typo
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvMeta = TextView(this).apply {
            text = String.format(Locale.getDefault(), "₱%.2f  •  %d mins", price, duration)
            setTextColor("#888888".toColorInt()) // Fixed textColor error
            textSize = 13f
            setPadding(0, 4, 0, 0)
        }

        layout.addView(tvName)
        layout.addView(tvMeta)
        card.addView(layout)

        card.setOnClickListener {
            for (i in 0 until containerServices.childCount) {
                (containerServices.getChildAt(i) as MaterialCardView).strokeColor = "#33FFFFFF".toColorInt()
            }
            card.strokeColor = "#D4AF37".toColorInt()

            selectedServiceId = id
            selectedServiceName = name
            selectedServicePrice = price
        }

        containerServices.addView(card)
    }

    private fun handleWizardNext() {
        when (viewFlipper.displayedChild) {
            0 -> {
                if (selectedServiceId == -1) {
                    Toast.makeText(this, "Please select a service first", Toast.LENGTH_SHORT).show()
                    return
                }
                viewFlipper.displayedChild = 1
                tvStepNumber.text = "STEP 2 OF 3"
                tvStepTitle.text = "Select Schedule"
                updateProgressBarWidth(0.66f)
            }
            1 -> {
                val dateInput = etBookingDate.text.toString().trim()
                val timeInput = etBookingTime.text.toString().trim()

                if (dateInput.isEmpty() || timeInput.isEmpty()) {
                    Toast.makeText(this, "Please fulfill date and time fields", Toast.LENGTH_SHORT).show()
                    return
                }

                // Resolves string concatenation warnings for UI elements
                summaryService.text = String.format(Locale.getDefault(), "Service: %s", selectedServiceName)
                summaryDateTime.text = String.format(Locale.getDefault(), "Schedule: %s at %s", dateInput, timeInput)
                summaryPrice.text = String.format(Locale.getDefault(), "₱%.2f", selectedServicePrice)

                viewFlipper.displayedChild = 2
                tvStepNumber.text = "STEP 3 OF 3"
                tvStepTitle.text = "Review Booking"
                btnWizardNext.text = "CONFIRM BOOKING"
                updateProgressBarWidth(1.0f)
            }
            2 -> {
                executePostBookingSubmission()
            }
        }
    }

    private fun executePostBookingSubmission() {
        val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("user", null) ?: return
        val userObj = JSONObject(userJson)
        val userId = userObj.optInt("userId", userObj.optInt("id", 1))

        val bookingPayload = JSONObject().apply {
            put("customerId", userId)
            put("serviceId", selectedServiceId)
            put("barberId", 1)
            put("scheduledAt", String.format(Locale.getDefault(), "%sT%s:00", etBookingDate.text, etBookingTime.text))
            put("status", "PENDING")
        }

        val request = JsonObjectRequest(Request.Method.POST, submitBookingUrl, bookingPayload,
            { _ ->
                Toast.makeText(this, "Booking Successful!", Toast.LENGTH_LONG).show()
                finish()
            },
            { error ->
                Log.e("PATUPI_BOOKING", "Submission Error: ${error.message}")
                Toast.makeText(this, "Failed to submit booking. Check connection.", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun handleWizardBack() {
        when (viewFlipper.displayedChild) {
            0 -> finish()
            1 -> {
                viewFlipper.displayedChild = 0
                tvStepNumber.text = "STEP 1 OF 3"
                tvStepTitle.text = "Select Service"
                updateProgressBarWidth(0.33f)
            }
            2 -> {
                viewFlipper.displayedChild = 1
                tvStepNumber.text = "STEP 2 OF 3"
                tvStepTitle.text = "Select Schedule"
                btnWizardNext.text = "NEXT STEP"
                updateProgressBarWidth(0.66f)
            }
        }
    }

    private fun updateProgressBarWidth(percentage: Float) {
        val params = progressBarFill.layoutParams as LinearLayout.LayoutParams
        params.weight = percentage
        progressBarFill.layoutParams = params
    }
}