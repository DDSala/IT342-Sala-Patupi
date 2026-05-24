package com.sala.patupi

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import androidx.core.view.isVisible

@Suppress("SetTextI18n", "SpellCheckingInspection")
class BookingActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var tvStepNumber: TextView
    private lateinit var tvStepTitle: TextView
    private lateinit var btnWizardNext: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var progressBarFill: View
    private lateinit var progressTrack: View
    private lateinit var containerServices: LinearLayout
    private lateinit var gridTimeSlots: GridLayout

    private lateinit var etAdditionalInstructions: EditText
    private lateinit var etBookingDate: EditText

    private lateinit var summaryService: TextView
    private lateinit var summaryDateTime: TextView
    private lateinit var summaryInstructions: TextView
    private lateinit var summaryPrice: TextView

    private lateinit var finalDateValue: TextView
    private lateinit var finalTimeValue: TextView

    private var selectedServiceId: Int = -1
    private var selectedServiceName: String = ""
    private var selectedServicePrice: Double = 0.0
    private var appointmentId: Int = -1
    private var userId: Int = 1

    private var selectedDisplayTime: String = ""
    private var selectedIsoTime: String = ""

    private val baseUrl = "http://192.168.1.2:8080/api"

    // Raw dataset array cleanly grouped chronologically
    private val timeSlots = arrayOf(
        "09:00 AM", "10:00 AM", "11:00 AM",
        "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM",
        "05:00 PM", "06:00 PM", "07:00 PM"
    )

    // A collection tracking references to buttons across categories for selection resets
    private val generatedTimeButtons = mutableListOf<MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        extractSessionUserId()
        initializeViews()
        setupBackPressedDispatcher()
        setupDatePicker()
        generateTimeSlotChips()

        fetchServicesFromDatabase()
    }

    private fun extractSessionUserId() {
        val userObj = SessionManager.currentUserJson
        if (userObj != null) {
            userId = userObj.optInt("userId", userObj.optInt("id", 1))
        } else {
            val sharedPref = getSharedPreferences("PatupiPrefs", Context.MODE_PRIVATE)
            val userJson = sharedPref.getString("user", null)
            if (userJson != null) {
                val fallbackObj = JSONObject(userJson)
                userId = fallbackObj.optInt("userId", fallbackObj.optInt("id", 1))
            }
        }
    }

    private fun initializeViews() {
        viewFlipper = findViewById(R.id.viewFlipper)
        tvStepNumber = findViewById(R.id.tvStepNumber)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        btnWizardNext = findViewById(R.id.btnWizardNext)
        btnBack = findViewById(R.id.btnBack)
        progressBarFill = findViewById(R.id.progressBarFill)
        progressTrack = findViewById(R.id.progressTrack)
        containerServices = findViewById(R.id.containerServices)
        gridTimeSlots = findViewById(R.id.gridTimeSlots)

        etAdditionalInstructions = findViewById(R.id.etAdditionalInstructions)
        etBookingDate = findViewById(R.id.etBookingDate)

        summaryService = findViewById(R.id.summaryService)
        summaryDateTime = findViewById(R.id.summaryDateTime)
        summaryInstructions = findViewById(R.id.summaryInstructions)
        summaryPrice = findViewById(R.id.summaryPrice)

        finalDateValue = findViewById(R.id.finalDateValue)
        finalTimeValue = findViewById(R.id.finalTimeValue)

        btnBack.setOnClickListener { handleWizardBack() }
        btnWizardNext.setOnClickListener { handleWizardNext() }
    }

    private fun setupBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleWizardBack()
            }
        })
    }

    private fun setupDatePicker() {
        etBookingDate.isFocusable = false
        etBookingDate.setOnClickListener {
            val c = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                android.R.style.Theme_Material_Dialog_Alert,
                { _, year, month, day ->
                    etBookingDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day))
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun generateTimeSlotChips() {
        gridTimeSlots.removeAllViews()
        generatedTimeButtons.clear()

        // Explicitly enforce that the grid layout container operates on a 3-column matrix system
        gridTimeSlots.columnCount = 3

        val morningSlots = mutableListOf<String>()
        val afternoonSlots = mutableListOf<String>()
        val eveningSlots = mutableListOf<String>()

        for (slot in timeSlots) {
            when (getSlotPeriodCategory(slot)) {
                "MORNING" -> morningSlots.add(slot)
                "AFTERNOON" -> afternoonSlots.add(slot)
                else -> eveningSlots.add(slot)
            }
        }

        appendTimeCategoryGroup("Morning Sessions", morningSlots)
        appendTimeCategoryGroup("Afternoon Sessions", afternoonSlots)
        appendTimeCategoryGroup("Evening Sessions", eveningSlots)
    }

    private fun getSlotPeriodCategory(slot: String): String {
        val spaceIdx = slot.indexOf(' ')
        val colonIdx = slot.indexOf(':')
        var hour = slot.substring(0, colonIdx).toInt()
        val amPm = slot.substring(spaceIdx + 1).uppercase(Locale.getDefault())

        if (amPm == "PM" && hour != 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0

        return when {
            hour < 12 -> "MORNING"
            hour < 17 -> "AFTERNOON"
            else -> "EVENING"
        }
    }

    private fun appendTimeCategoryGroup(title: String, slots: List<String>) {
        if (slots.isEmpty()) return

        // --- FIXED: Inject header layout parameters with full column-span (0 to 3) configuration ---
        val headerLabel = TextView(this).apply {
            text = title
            setTextColor("#D4AF37".toColorInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(8, 36, 0, 16)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                // Span header label completely across all 3 grid paths
                columnSpec = GridLayout.spec(0, 3, 1f)
            }
            layoutParams = params
        }
        gridTimeSlots.addView(headerLabel)

        // --- FIXED: Inject elements directly into the GridLayout using individual weights ---
        for (slot in slots) {
            val themedContext = android.view.ContextThemeWrapper(this, androidx.appcompat.R.style.Widget_AppCompat_Button_Borderless)
            val btnSlot = MaterialButton(themedContext).apply {
                text = slot
                textSize = 11f
                setTextColor("#FFFFFF".toColorInt())
                strokeColor = android.content.res.ColorStateList.valueOf("#33FFFFFF".toColorInt())
                strokeWidth = 2
                cornerRadius = 12
                backgroundTintList = android.content.res.ColorStateList.valueOf("#1E1E1E".toColorInt())
                setPadding(0, 12, 0, 12)

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    setMargins(6, 6, 6, 6)
                }
                layoutParams = params
            }

            btnSlot.setOnClickListener {
                for (btn in generatedTimeButtons) {
                    btn.strokeColor = android.content.res.ColorStateList.valueOf("#33FFFFFF".toColorInt())
                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf("#1E1E1E".toColorInt())
                    btn.setTextColor("#FFFFFF".toColorInt())
                }
                btnSlot.strokeColor = android.content.res.ColorStateList.valueOf("#D4AF37".toColorInt())
                btnSlot.backgroundTintList = android.content.res.ColorStateList.valueOf("#2A2415".toColorInt())
                btnSlot.setTextColor("#D4AF37".toColorInt())

                selectedDisplayTime = slot
                selectedIsoTime = convert12To24HourFormat(slot)
            }

            gridTimeSlots.addView(btnSlot)
            generatedTimeButtons.add(btnSlot)
        }
    }

    private fun convert12To24HourFormat(time12: String): String {
        val spaceIndex = time12.indexOf(' ')
        val colonIndex = time12.indexOf(':')

        var hour = time12.substring(0, colonIndex).toInt()
        val minute = time12.substring(colonIndex + 1, spaceIndex)
        val amPm = time12.substring(spaceIndex + 1)

        if (amPm == "PM" && hour != 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0

        return String.format(Locale.getDefault(), "%02d:%s:00", hour, minute)
    }

    private fun fetchServicesFromDatabase() {
        val request = JsonArrayRequest(Request.Method.GET, "$baseUrl/services", null,
            { response ->
                containerServices.removeAllViews()
                for (i in 0 until response.length()) {
                    createServiceCardRow(response.getJSONObject(i))
                }
            },
            { error ->
                Log.e("PATUPI_BOOKING", "Failed to retrieve services: ${error.message}")
                Toast.makeText(this, "Error fetching services.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun createServiceCardRow(service: JSONObject) {
        val id = service.getInt("service_id")
        val name = service.getString("name")
        val price = service.getDouble("base_price")
        val duration = service.getInt("duration_minutes")

        val card = MaterialCardView(this).apply {
            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
            radius = 16f
            setCardBackgroundColor("#1E1E1E".toColorInt())
            strokeWidth = 2
            strokeColor = "#33FFFFFF".toColorInt()
            isClickable = true
            isFocusable = true
        }

        val layout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        val tvName = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            text = name
            setTextColor("#FFFFFF".toColorInt())
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvMeta = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            text = String.format(Locale.getDefault(), "₱%.2f  •  %d mins", price, duration)
            setTextColor("#888888".toColorInt())
            textSize = 13f
            setPadding(0, 6, 0, 0)
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
                val customInstructions = etAdditionalInstructions.text.toString().trim()
                if (selectedServiceId == -1 && customInstructions.isEmpty()) {
                    Toast.makeText(this, "Please select a service or describe your look", Toast.LENGTH_SHORT).show()
                    return
                }
                executeStep1Initialization()
            }
            1 -> {
                val dateInput = etBookingDate.text.toString().trim()
                if (dateInput.isEmpty() || selectedIsoTime.isEmpty()) {
                    Toast.makeText(this, "Please select both a date and a time slot.", Toast.LENGTH_SHORT).show()
                    return
                }

                summaryService.text = "Service: ${selectedServiceName.ifEmpty { "Custom Style" }}"
                summaryDateTime.text = "Schedule: $dateInput at $selectedDisplayTime"
                summaryInstructions.text = "Note: ${etAdditionalInstructions.text.toString().trim().ifEmpty { "None" }}"
                summaryPrice.text = if (selectedServicePrice > 0) String.format(Locale.getDefault(), "₱%.2f", selectedServicePrice) else "To be quoted"

                viewFlipper.displayedChild = 2
                tvStepNumber.text = "STEP 3 OF 3"
                tvStepTitle.text = "Review Booking"
                btnWizardNext.text = "CONFIRM & BOOK"
                updateProgressBarWidth(1.0f)
            }
            2 -> executeStep3PutConfirmation()
            3 -> finish()
        }
    }

    private fun executeStep1Initialization() {
        val customInstructions = etAdditionalInstructions.text.toString().trim()
        val payload = JSONObject().apply {
            put("customerId", userId)
            put("description", customInstructions.ifEmpty { "No description provided" })
            put("serviceId", if (selectedServiceId == -1) JSONObject.NULL else selectedServiceId)
        }

        btnWizardNext.isEnabled = false
        btnWizardNext.text = "INITIALIZING..."

        val request = JsonObjectRequest(Request.Method.POST, "$baseUrl/appointments/step1", payload,
            { response ->
                btnWizardNext.isEnabled = true
                btnWizardNext.text = "NEXT STEP"
                appointmentId = response.getInt("appointmentId")

                viewFlipper.displayedChild = 1
                tvStepNumber.text = "STEP 2 OF 3"
                tvStepTitle.text = "Pick A Schedule"
                updateProgressBarWidth(0.66f)
            },
            { error ->
                btnWizardNext.isEnabled = true
                btnWizardNext.text = "NEXT STEP"
                Log.e("PATUPI_BOOKING", "Step 1 error: ${error.message}")
                Toast.makeText(this, "Failed to start booking.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun executeStep3PutConfirmation() {
        if (appointmentId == -1) {
            Toast.makeText(this, "Session lost. Please restart booking.", Toast.LENGTH_SHORT).show()
            return
        }

        val payload = JSONObject().apply {
            put("serviceId", if (selectedServiceId == -1) JSONObject.NULL else selectedServiceId)
            put("scheduledAt", "${etBookingDate.text.toString().trim()}T$selectedIsoTime")
        }

        btnWizardNext.isEnabled = false
        btnWizardNext.text = "PROCESSING..."

        val request = JsonObjectRequest(Request.Method.PUT, "$baseUrl/appointments/confirm/$appointmentId", payload,
            { _ ->
                btnWizardNext.isEnabled = true
                finalDateValue.text = "DATE: ${etBookingDate.text.toString().trim()}"
                finalTimeValue.text = "TIME: $selectedDisplayTime"

                findViewById<View>(R.id.bookingHeader).isVisible = false
                progressTrack.isVisible = false

                viewFlipper.displayedChild = 3
                btnWizardNext.text = "BACK TO DASHBOARD"
            },
            { error ->
                btnWizardNext.isEnabled = true
                btnWizardNext.text = "CONFIRM & BOOK"
                Log.e("PATUPI_BOOKING", "Confirmation error: ${error.message}")
                Toast.makeText(this, "Could not finish booking.", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun handleWizardBack() {
        when (viewFlipper.displayedChild) {
            0 -> finish()
            1 -> executeMidWizardCancellation()
            2 -> {
                viewFlipper.displayedChild = 1
                tvStepNumber.text = "STEP 2 OF 3"
                tvStepTitle.text = "Pick A Schedule"
                btnWizardNext.text = "NEXT STEP"
                updateProgressBarWidth(0.66f)
            }
            3 -> finish()
        }
    }

    private fun executeMidWizardCancellation() {
        if (appointmentId == -1) {
            returnToStep0()
            return
        }

        val request = StringRequest(Request.Method.DELETE, "$baseUrl/appointments/$appointmentId",
            {
                appointmentId = -1
                returnToStep0()
            },
            { error ->
                Log.w("PATUPI_BOOKING", "Clean-up rejected: ${error.message}")
                returnToStep0()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun returnToStep0() {
        viewFlipper.displayedChild = 0
        tvStepNumber.text = "STEP 1 OF 3"
        tvStepTitle.text = "Style selection"
        updateProgressBarWidth(0.33f)
    }

    private fun updateProgressBarWidth(percentage: Float) {
        val params = progressBarFill.layoutParams as LinearLayout.LayoutParams
        params.weight = percentage
        progressBarFill.layoutParams = params
    }
}