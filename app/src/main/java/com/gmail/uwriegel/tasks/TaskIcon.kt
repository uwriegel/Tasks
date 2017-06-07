package com.gmail.uwriegel.tasks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by urieg on 07.06.2017.
 */
class TaskIcon : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, paint)
    }
}