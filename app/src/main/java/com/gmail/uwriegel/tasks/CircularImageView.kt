package com.gmail.uwriegel.tasks

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class CircularImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {

        val drawable = drawable ?: return

        if (width == 0 || height == 0)
            return

        val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val w = width/*, h = getHeight( )*/

        val roundBitmap = getCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)
    }

    private fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
        val bitmap = if (bmp.width != radius || bmp.height != radius) {
            val smallest = Math.min(bmp.width, bmp.height).toFloat()
            val factor = smallest / radius
            Bitmap.createScaledBitmap(bmp, (bmp.width / factor).toInt(), (bmp.height / factor).toInt(), false)
        } else
            bmp

        val output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, radius, radius)

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.parseColor("#BAB399")
        canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f, radius / 2 + 0.1f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}