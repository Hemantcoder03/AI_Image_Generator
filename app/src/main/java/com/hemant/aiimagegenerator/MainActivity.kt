package com.hemant.aiimagegenerator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    //creating an variables
    lateinit var image: ImageView
    lateinit var loadingImg: SpinKitView
    lateinit var imageText: EditText
    lateinit var downloadBtn: MaterialButton
    lateinit var shareBtn: MaterialButton
    var url = "https://api.openai.com/v1/images/generations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initializing variables
        image = findViewById(R.id.image);
        loadingImg = findViewById(R.id.loadingImg);
        imageText = findViewById(R.id.imageText);
        downloadBtn = findViewById(R.id.downloadBtn);
        shareBtn = findViewById(R.id.shareBtn);

        //add listener to the edittext
        imageText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //check text is empty or not
                if (imageText.text.trim().isEmpty()) {
                    //hiding the loading image
                    loadingImg.visibility = View.GONE
                } else {
                    //set loading image visible
                    loadingImg.visibility = View.VISIBLE
                    sendRequest(imageText.text.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        downloadBtn.setOnClickListener(object : OnClickListener {
            override fun onClick(p0: View?) {

                //permission granted
                //call the method to create pdf
                Toast.makeText(
                    this@MainActivity,
                    "Downloaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        shareBtn.setOnClickListener(object : OnClickListener {
            override fun onClick(p0: View?) {

                shareImg()
            }
        })
    }

    private fun sendRequest(text: String) {

        //creating queue for  request queue
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        //creating jsonobject
        val jsonObject: JSONObject = JSONObject()
        //put the values
        jsonObject.put("prompt", text)
        jsonObject.put("n", 1)
        jsonObject.put("size", "512x512")

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject, Response.Listener { response ->
                var imageURL: String =
                    response.getJSONArray("data").getJSONObject(0).getString("url")
                imageURL = imageURL.replace("\\", "")

                // using picasso to load image from url in image view
                Picasso.get().load(imageURL).into(image)
                //hiding the loading image
                loadingImg.visibility = View.GONE
            },
                Response.ErrorListener {
                    //error message
                    Toast.makeText(applicationContext, "your limit reached", Toast.LENGTH_LONG)
                        .show()
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] =
                        "Bearer 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"
                    return params
                }
            }

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // on below line adding our request to queue.
        queue.add(postRequest)
    }


    private fun shareImg() {
        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path)
        try {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.packageName + ".provider",
                    file
                )
            )
            shareIntent.type = "application/jpg"
            startActivity(shareIntent)

        } catch (e: Exception) {
            Log.d("error is", e.message!!)
        }
    }
}