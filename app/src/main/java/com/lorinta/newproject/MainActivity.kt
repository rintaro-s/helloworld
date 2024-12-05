package com.lorinta.newproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lorinta.newproject.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException

private val discordWebhookURL = "https://discord.com/api/webhooks/1311135534726254613/XYf8c_tHwsrMxfThkxjRyeWL9P-kchu9fIuMf0NVJkJ6_a7zk0U5Fmz62ed8xO_Vjvvc" // Set your Discord webhook URL here

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val lineNotifyToken = "line notify token" // Set your LINE Notify token here
    private val PREFS_NAME = "MyPrefsFile"
    private val AGREED_TO_TOS = "agreedToTos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasAgreedToTos = sharedPreferences.getBoolean(AGREED_TO_TOS, false)

        if (!hasAgreedToTos) {
            // Display terms of service
            binding.tosText.text = """
利用規約

第1条 (目的)
本利用規約は、当方が提供するサービスの利用条件を定めるものです。

第2条 (適用範囲)
本利用規約は、当方のサービスを利用するすべての方に適用されます。

第3条 (サービスの内容)
1. 当方が提供するサービスの内容は、当方の判断により変更される場合があります。
2. サービスの内容、仕様、機能等は予告なく変更される場合があります。

第4条 (利用者の責任)
1. サービスの利用は全て利用者の責任において行われるものとします。
2. 未成年の方は保護者の同意を得た上で利用してください。
3. 利用者は関連法規を遵守し、他者の権利を侵害しない方法でサービスを利用するものとします。

第5条 (免責事項)
1. 当方は、サービスの中断、停止、終了により生じた損害について一切責任を負いません。
2. サービスの利用により生じた損害について、当方は一切責任を負いません。
3. 当方は、サービスの内容、仕様、機能等の変更により生じた損害について一切責任を負いません。

第6条 (知的財産権)
サービスに含まれる著作権、商標権等の知的財産権は当方または当方にライセンスを許諾している第三者に帰属します。

第7条 (禁止事項)
利用者は、以下の行為を行ってはなりません。
- 法令または公序良俗に違反する行為
- 犯罪行為に関連する行為
- 本サービスの内容等、本サービスに含まれる著作権、商標権ほか知的財産権を侵害する行為
- 当方、ほかのユーザー、またはその他第三者のサーバーまたはネットワークの機能を破壊したり、妨害したりする行為
- 本サービスによって得られた情報を商業的に利用する行為
- 当方のサービスの運営を妨害するおそれのある行為
- 不正アクセスをし、またはこれを試みる行為
- 他のユーザーに関する個人情報等を収集または蓄積する行為
- 不正な目的を持って本サービスを利用する行為
- 本サービスの他のユーザーまたはその他の第三者に不利益、損害、不快感を与える行為
- 他のユーザーに成りすます行為
- 当方が許諾しない本サービス上での宣伝、広告、勧誘、または営業行為
- 面識のない異性との出会いを目的とした行為
- 当方のサービスに関連して、反社会的勢力に対して直接または間接に利益を供与する行為
- その他、当方が不適切と判断する行為

第8条 (利用の停止・制限)
当方は、利用者が本規約に違反した場合、事前の通知なくサービスの利用を停止または制限することができます。

第9条（保証の否認および免責事項）
当方は、本サービスに事実上または法律上の瑕疵（安全性、信頼性、正確性、完全性、有効性、特定の目的への適合性、セキュリティなどに関する欠陥、エラーやバグ、権利侵害などを含みます。）がないことを明示的にも黙示的にも保証しておりません。
当方は、本サービスに起因してユーザーに生じたあらゆる損害について、当方の故意又は重過失による場合を除き、一切の責任を負いません。ただし、本サービスに関する当方とユーザーとの間の契約（本規約を含みます。）が消費者契約法に定める消費者契約となる場合、この免責規定は適用されません。
前項ただし書に定める場合であっても、当方は、当方の過失（重過失を除きます。）による債務不履行または不法行為によりユーザーに生じた損害のうち特別な事情から生じた損害（当方またはユーザーが損害発生につき予見し、または予見し得た場合を含みます。）について一切の責任を負いません。
また、当方の過失（重過失を除きます。）による債務不履行または不法行為によりユーザーに生じた損害の賠償は、ユーザーから当該損害が発生した月に受領した利用料の額を上限とします。
当方は、本サービスに関して、ユーザーと他のユーザーまたは第三者との間において生じた取引、連絡または紛争等について一切責任を負いません。


第10条 (準拠法・管轄裁判所)
本利用規約の解釈及び適用については日本法が適用されるものとします。本サービスに関して紛争が生じた場合には、当方の本店所在地を管轄する裁判所を専属的合意管轄とします。

以上

            Shigeno Rintaro 2024
        """
            binding.agreeButton.isEnabled = true

            binding.agreeButton.setOnClickListener {
                // User agreed to the terms
                sharedPreferences.edit().putBoolean(AGREED_TO_TOS, true).apply()
                displayHelloWorldAndButton()
            }
        } else {
            // User has already agreed to the terms
            displayHelloWorldAndButton()
        }
    }

    private fun displayHelloWorldAndButton() {
        binding.tosText.text = "helloworld"
        binding.agreeButton.text = "ボタン"
        binding.agreeButton.isEnabled = true

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else {
            startLocationService()
        }

        // Set up button click listener
        binding.agreeButton.setOnClickListener {
            sendDistanceViaDiscord() // Send the current distance via Discord webhook
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService()
        } else {
            sendErrorViaDiscord("Location permission denied")
        }
    }

    private fun startLocationService() {
        try {
            startForegroundService(Intent(this, LocationService::class.java))
        } catch (e: Exception) {
            sendErrorViaDiscord("Failed to start service: ${e.message}")
        }
    }

    private fun sendDistanceViaDiscord() {
        // Send the current distance via Discord webhook
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("content", "Current distance from the target is X meters.") // Replace X with the actual distance
            .build()
        val request = Request.Builder()
            .url(discordWebhookURL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                sendErrorViaDiscord("Failed to send distance via Discord: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // Message sent successfully
            }
        })
    }

    private fun sendErrorViaDiscord(error: String) {
        // Notify error via Discord webhook
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("content", "Error: $error")
            .build()
        val request = Request.Builder()
            .url(discordWebhookURL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Error message sent successfully
            }
        })
    }
}