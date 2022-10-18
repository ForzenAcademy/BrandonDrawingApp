package com.example.drawingactivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * This Class is inherits the Glide Class Custom Target and allows you to set it to a bitmap.
 * onResourceReady executes when the load operation is done.
 * onLoadFailed will load an error drawable as a bitmap to set, alerting the user that an error occurred.
 */
class BitmapGetter(val onComplete: (Bitmap) -> Unit, val onError: (Bitmap) -> Unit) :
    CustomTarget<Bitmap>() {

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        onComplete(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {}

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        val bitmap = errorDrawable?.toBitmap()
        bitmap?.let { onError(bitmap) }
    }
}

/**
 * Shorthand Class to insert a bitmap into a location that accepts a bitmap using Glide.
 * onComplete determines what is done with the Bitmap obtained from the URI.
 * onError takes in an error bitmap and will output it however the user decides.
 * When this function fails to get the image, it will toast a generic error.
 */
class BitmapFromUri(
    context: Context, url: Uri, val onComplete: (Bitmap) -> Unit, val onError: (Bitmap) -> Unit
) {
    init {
        Glide.with(context).asBitmap().load(url)
            .into(BitmapGetter(onComplete = { onComplete(it) }, onError = {
                Toast.makeText(
                    context,
                    context.getString(R.string.errorImageGrabberFailure),
                    Toast.LENGTH_LONG
                ).show()
                onError(it)
            }))
    }
}