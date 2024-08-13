package com.asadbek.fingerprinttutorial

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.asadbek.fingerprinttutorial.databinding.ActivityMainBinding

/**
 *  Owner: Dominic Azimov
 *  Release data: 2024.08.12
 *  Steps: notifyUser,cancellationSignal,authenticationCallback,checkBiometricSupport,getCancelledSignal,BtnListener
 *  In the manifest must be: BIOMETRIC Permission
 *  Min level api version: 28
 */

class MainActivity : AppCompatActivity() {
    // barmoq skaneri bekor qilindagi ishlovchi o`zgaruvchi
    private var cancellationSignal:CancellationSignal? = null

    // barmoq izi tanib olinganligi yoki olinmaganligi haqidagi listener callbacki
    private val authenticationCallback:BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object :BiometricPrompt.AuthenticationCallback(){
                //barmoq izi tanib olinmasa ishlaydi
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error: $errString")
                }


                // barmoq izi tanib olinsa ishlaydi
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication success! Barmoq izi tanib olindi!")
                    val intent = Intent(this@MainActivity,PrivateActivity::class.java)
                    startActivity(intent)
                }
            }
    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.P) // api 28 va undan katta verisyalar uchun ishlaydi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        checkBiometricSupport()

        binding.btnAuth.setOnClickListener {
            val biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Title of prompt") // title - nomi
                .setSubtitle("Authentication is required") // nima uchun kerak so`ralayotganligi
                .setDescription("This app uses fingerprint protection to keep your data secure") // barmoq izi talab qilinishi sababi
                .setNegativeButton("Cancel",this.mainExecutor,DialogInterface.OnClickListener{ dialog, which ->
                    notifyUser("Authentication cancelled") // bekor qilinganda ishlaydi
                }).build()
            biometricPrompt.authenticate(getCancellationSignal(),mainExecutor,authenticationCallback)
        }
    }

    // barmoq izi skanerlashi bekor qilinganda ishlaydi
    private fun getCancellationSignal():CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication has cancelled by user")
        }
        return cancellationSignal as CancellationSignal
    }
    // telefonda biometrik skanerlash bor yoki yo`qligi va dastur uchun ruxsat borligini tekshiradi
    private fun checkBiometricSupport():Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // barmoq izi mavjud bo`lmasa ishlaydi
        if (!keyguardManager.isKeyguardSecure){
            notifyUser("Fingerprint authentication has not been enabled in settings")
            return false
        }
        // ruxsat yo`q bo`lsa ishlaydu
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint authentication permission is not enabled")
            return false
        }
        // barmoq izi mavjud bo`lsa ishlaydi
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }else true
    }

    private fun notifyUser(message:String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}