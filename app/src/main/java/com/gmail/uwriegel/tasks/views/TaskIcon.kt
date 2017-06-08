package com.gmail.uwriegel.tasks.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

/**
 * Created by urieg on 07.06.2017.
 */
class TaskIcon : View {

    init{
        this.paint = setColor()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, paint)


        var paintStroke = Paint()
        paintStroke.style = Paint.Style.STROKE
        paintStroke.color = Color.DKGRAY
        paintStroke.isAntiAlias = true
        //canvas.drawCircle(width / 2f, width / 2f, width / 2f, paintStroke)

        val paint = Paint()
        paint.setTextSize(40f);
        paint.setColor(Color.WHITE);
        paint.isAntiAlias = true
        paint.setTextAlign(Paint.Align.CENTER);

        val text = "So"
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds);
        canvas.drawText(text, width / 2f, width / 2f - textBounds.exactCenterY(), paint);
    }

    fun setColor(color: Int = Color.RED): Paint {
        var paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.isAntiAlias = true
        return paint
    }

    var paint: Paint
}