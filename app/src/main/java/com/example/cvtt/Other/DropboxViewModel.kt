package com.example.cvtt.Other

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException

class DropboxViewModel : ViewModel() {

    // Thay bằng token mới từ Dropbox App Console
val code="sl.u.AFvVMkGHQMT32KxEcPpn8FSvKYHteGM8LoEuAxv18l0bOS2XvDu5GIuCyX3y-ZB1UGyyXthechLE-8GrvPQXe9LLRQU8EBPCPTKARY_3r4Zij07pAGBVnWi3ANOBgs1iDSRVjHvh-PXzqZ51VYiRmrea0iLfFdzcvPNZcKWDIfKkOOlyI4Ta2cCXKLu10FYSfAYMHrl1EwSzD-dIYITpyghgb5OhysOSXHDLxhV28qbCkUXMCJrUCu-grlhaXPkNOe5Q4dVxgz4x8enWMEEq7PTAenurHlGCUWHe7YXk-SUSgV5LVZsne-b4ANofowNC6CSYdXNhxEP5jZ3BbC1Fhx17w3r3sNAJwdM5cVASomZXCYkOUYukdEI4wgB2_U7UJKhvVxWNM4ukuut_cnfi3k0YEQHFpttUW4eqQ2wrepmAcHTGGXJllxp940jao4y940AshTK4gcBBpQXdcwyfo-G52HgjSyYrAdnncAgF_9w1PLHlza9_o81OuLVTdDsEf7AEn6gZhEr8jXRPQewtAZSawLtHRckoMOu_01SWHcYAZorKCGHsZNX0r4ELSAx9tLZA8AZITQA-4BInh_giudGTINrF3xu2YrC13Rb4jlQUj6gtKw9FOE6tQoAO7Dr2qQklPFcDj2jB5pxG-MFJMLsbPPqXGRYoEgyfxUDD_CSl-YkepltWmzkKxJE_0MiHukhJ9cuLFUpEk5fM7EKqwnojQCTq-5UsWm2vDZDcryO-QFq3YdbyaJOhgWD_fKT90XEPkEFl9UDyB62dwr1vK71CrId6WnoEfymMaUzfLK169G_jS-7UeWUb1rnnzUlxxKuE5-hJ7nE-_NqR4kUt68vG0usRzTAY3_Qh8txj3H3l_eLxcmVVdgIceWWSDOTLBUgNHBwSYQgI8DP0jGP_dgX0Bidku6FliwHADCEQJSBYFOymNkdBh9rlKWFGsUdYelhDETHOg_JYC76tMxJN6_vAqVP0EF-zRQ7xEkjgolBIlh4afP0AOG_Ahv2EZdO28bIz7BJNQpar4PM21nKaBhJTJOxGri7gEAsLsbxKEBr2QiR1X3ZgDLomUwiKzR-6_eRaS7Qz6vu4O1z1rDcqmSpLxrd4E3MVD4-CRpduSpiDRB9hCAcDnP9_cIoO_G6C9LCbMLGw1PW_binePTXW0_5HNGfmld3wEldqgACW-itEb7OesMrd1OKtVYSZiQSk-IkyzlPug-HoRzsTqfrs9g_1qvl2bqYMznpOdINUAQ0e9xOSwKs5iMKP-TDvBlLlc5l7Q4NMtBv3aUGXlgyeYaV7ywR_vP9gQ1N-2wJrG87irP9agLUtwytz8kFRDOxjSDm25gQsaAiQ6HMjV1XjbSXKq2j7wSHXne1Ue0kPh_KCYEwbECl_qjXRoXGsxJYeT6oVnKnCynUmVcC98HmJ1V8Mf6X8N6P_so51-doNSwr-DA"
private val dropboxAccessToken =code

    fun uploadFileToDropbox(
        context: Context,
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lấy tên file và loại MIME từ URI
                val fileExtension = getFileExtension(context.contentResolver, fileUri)
                val fileName = "cv_${System.currentTimeMillis()}.$fileExtension" // Tạo tên file duy nhất và thêm đuôi

                val inputStream = context.contentResolver.openInputStream(fileUri)
                    ?: throw IOException("Cannot open input stream")

                // Tạo RequestBody để stream file
                val requestBody = object : RequestBody() {
                    override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                    override fun writeTo(sink: BufferedSink) {
                        inputStream.use { it.copyTo(sink.outputStream()) }
                    }
                }

                // Tạo Dropbox-API-Arg header
                val dropboxApiArg = JSONObject().apply {
                    put("path", "/$fileName")
                    put("mode", "add")
                    put("autorename", true)
                    put("mute", false)
                }.toString()

                // Tạo request upload
                val request = Request.Builder()
                    .url("https://content.dropboxapi.com/2/files/upload")
                    .addHeader("Authorization", "Bearer $dropboxAccessToken")
                    .addHeader("Dropbox-API-Arg", dropboxApiArg)
                    .addHeader("Content-Type", "application/octet-stream")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("DropboxUpload", "Upload failed", e)
                        onFailure("Upload failed: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            createSharedLink(fileName, onSuccess, onFailure)
                        } else {
                            val error = response.body?.string() ?: "Unknown error"
                            Log.e("DropboxUpload", "Upload failed: ${response.code} - $error")
                            onFailure("Upload failed: $error")
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("DropboxUpload", "Error", e)
                onFailure("Error: ${e.message}")
            }
        }

    }

    private fun createSharedLink(fileName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val requestBody = JSONObject().apply {
            put("path", "/$fileName")
            put("settings", JSONObject().apply {
                put("requested_visibility", "public")
            })
        }.toString()

        val request = Request.Builder()
            .url("https://api.dropboxapi.com/2/sharing/create_shared_link_with_settings")
            .addHeader("Authorization", "Bearer $dropboxAccessToken")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DropboxLink", "Link creation failed", e)
                onFailure("Link creation failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val url = json.optString("url", "")
                    if (url.isNotEmpty()) {
                        val directLink = url.replace("?dl=0", "?raw=1")
                        onSuccess(directLink)
                    } else {
                        Log.e("DropboxLink", "No URL in response: $responseBody")
                        onFailure("Failed to parse shared link")
                    }
                } else {
                    Log.e("DropboxLink", "Link creation failed: ${response.code} - $responseBody")
                    onFailure("Link creation failed: $responseBody")
                }
            }
        })
    }
}
fun getFileExtension(contentResolver: ContentResolver, fileUri: Uri): String {
    // Lấy MIME type của file
    val mimeType = contentResolver.getType(fileUri)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

    // Nếu không có, mặc định trả về "txt" (hoặc có thể thay đổi theo ý muốn)
    return extension ?: "txt"
}