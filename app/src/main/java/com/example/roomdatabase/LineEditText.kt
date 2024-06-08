package com.example.roomdatabase

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.EditText


/**
 * @author Phat ( Phillip ) H . VU <vuhongphat></vuhongphat>@hotmail.com>
 */
@SuppressLint("AppCompatCustomView")
class LineEditText(context: Context?, attrs: AttributeSet?) :
    EditText(context, attrs) {
    private val mRect: Rect
    private val mPaint: Paint

    init {
        mRect = Rect()
        mPaint = Paint()
        // define the style of line
        mPaint.style = Paint.Style.FILL_AND_STROKE
        // define the color of line
        mPaint.setColor(Color.BLACK)
    }

    override fun onDraw(canvas: Canvas) {
        val height = height
        val lHeight = lineHeight
        // the number of line
        var count = height / lHeight
        if (lineCount > count) {
            // for long text with scrolling
            count = lineCount
        }
        val r = mRect
        val paint = mPaint

        // first line
        var baseline = getLineBounds(0, r)

        // draw the remaining lines.
        for (i in 0 until count) {
            canvas.drawLine(
                r.left.toFloat(),
                (baseline + 1).toFloat(),
                r.right.toFloat(),
                (baseline + 1).toFloat(),
                paint
            )
            // next line
            baseline += lineHeight
        }
        super.onDraw(canvas)
    }
}