package mx.edu.ittepic.ladm_u4_pr1_almacensms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    var numero = ""
    var texto = ""
    var horayfecha = ""
    init {
        Manifest.permission.READ_PHONE_STATE
        Manifest.permission.SEND_SMS
        "Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE"

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        Manifest.permission.READ_PHONE_STATE
        Manifest.permission.SEND_SMS

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        telefono.setText("")
        mensaje.setText("")
        mostrar()

        enviar.setOnClickListener {
            numero = telefono.text.toString()
            texto = mensaje.text.toString()

            horayfecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a"))


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Ask for permision
                ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.SEND_SMS), 1)
                println("eop"+numero+texto)
                mandar(numero,texto)
            }
            else {
                // Permission has already been granted.
                println("ep"+numero+texto)
                mandar(numero,texto)
            }

        }
    }
    fun insertar(){
        var datos = hashMapOf(
            "fecha" to horayfecha.toString(),
            "mensaje" to texto,
            "telefono" to numero,
            "registrado" to Date()
        )
        
        FirebaseFirestore.getInstance()
            .collection("smsenviados")
            .add(datos)
            .addOnSuccessListener {
                telefono.setText("")
                mensaje.setText("")
            }
            .addOnFailureListener {
                aler(it.message!!)
            }
    }
    fun mandar(tel: String, msj: String){
        println(tel+msj)
        try {
            val smsManager: SmsManager
            if (Build.VERSION.SDK_INT>=23) { smsManager = this.getSystemService(SmsManager::class.java) }
            else{ smsManager = SmsManager.getDefault() }

            smsManager.sendTextMessage(tel,null,msj,null,null)
            toas("Mensaje enviado")
            insertar()
            telefono.setText("")
            mensaje.setText("")
        } catch (e: Exception) {
            toas("Por favor, revise..."+e.message.toString())
        }
    }

    private fun mostrar() {
        telefono.setText("")
        mensaje.setText("")
        FirebaseFirestore.getInstance()
            .collection("smsenviados")
            .addSnapshotListener { value, error ->
                if(error!=null){
                    aler("No fue posible realizar la consulta")
                    return@addSnapshotListener
                }
                var resultado = ArrayList<String>()
                for(documento in value!!){
                    val cad = "Enviado a: "+documento.getString("telefono")+"\n"+
                            "Mensaje: "+documento.get("mensaje")+"\n"+
                            "Fecha de envio: "+documento.getString("fecha");
                    resultado.add(cad)
                }//for

                lista.adapter = ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, resultado)

            }//firebase
    }

    fun toas(m:String){
        Toast.makeText(this,m, Toast.LENGTH_LONG).show()
    }

    fun aler(m:String){
        AlertDialog.Builder(this).setTitle("ATENCION")
            .setMessage(m)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}