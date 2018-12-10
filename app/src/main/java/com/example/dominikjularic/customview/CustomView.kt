package com.example.dominikjularic.customview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.BounceInterpolator

class CustomView : View {

    private var RECTANGLE_SIZE_DEFAULT : Int = 200
    private var mCanvasBackground: Int? = null
    private lateinit var mRectangleBackground: Rect
    private lateinit var mPaintRectangleBackground: Paint
    private var mStartingRectanglePositionTop: Int? = null
    private var mStartingRectanglePositionLeft: Int? = null
    private var mStartingRectanglePositionRight: Int? = null
    private var mStartingRectanglePositionBottom: Int? = null
    private var mStartingRectanglePositionLeftDefault: Int? = null
    private var mStartingRectanglePositionRightDefault: Int? = null
    private var mRectangleTransitionEndPositionRight: Int? = null
    private lateinit var mTextView: Paint
    private lateinit var mRectangle: Rect
    private lateinit var mPaintRectangle: Paint
    private var mPaintRectangleColorTransition: Int? = null
    private var mCustomViewHeight: Int? = null
    private var mCustomViewWidth: Int? = null
    enum class RectangleValues(val position: Int){
        TOP(0),
        LEFT(0),
    }
    enum class RectangleScale(val factor: Float){
        TOP(0.05f),
        LEFT(0.05f),
        RIGHT(0.45f),
        BOTTOM(0.95f),
        MAX_RECTANGLE_SIZE(0.40f),
        END_POSITION_RIGHT(0.95f)
    }
    private var mRectangleColor: Int? = null
    private var mRectangleSizeWidth: Int? = null

    constructor(context: Context?) : super(context){
        init(null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init(attrs)

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(attrs)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,attrs,defStyleAttr,defStyleRes){
        init(attrs)
    }

    fun init(set: AttributeSet?){

        mTextView = Paint()
        mRectangleBackground = Rect()
        mPaintRectangleBackground = Paint(Paint.ANTI_ALIAS_FLAG)
        mRectangle = Rect()
        mPaintRectangle= Paint(Paint.ANTI_ALIAS_FLAG)

        if (set == null)
            return

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                else
                    viewTreeObserver.removeGlobalOnLayoutListener(this)

                mCustomViewHeight = height
                mCustomViewWidth = width
                mStartingRectanglePositionTop = (height*RectangleScale.TOP.factor).toInt()
                mStartingRectanglePositionLeftDefault = (width*RectangleScale.LEFT.factor).toInt()
                mStartingRectanglePositionLeft = (width*RectangleScale.LEFT.factor).toInt()
                mStartingRectanglePositionBottom = (height*RectangleScale.BOTTOM.factor).toInt()

                if(mRectangleSizeWidth!! <= width*RectangleScale.MAX_RECTANGLE_SIZE.factor) {
                    mStartingRectanglePositionRight = mStartingRectanglePositionLeftDefault!! + mRectangleSizeWidth!!
                    mStartingRectanglePositionRightDefault = mStartingRectanglePositionLeftDefault!!+ mRectangleSizeWidth!!
                }
                else {
                    mStartingRectanglePositionRight = (width * RectangleScale.RIGHT.factor).toInt()
                    mStartingRectanglePositionRightDefault = (width * RectangleScale.RIGHT.factor).toInt()
                }
                mRectangleTransitionEndPositionRight = (width*RectangleScale.END_POSITION_RIGHT.factor).toInt()
            }
        })

        var attributes: TypedArray = context.obtainStyledAttributes(set,R.styleable.CustomView)

        mCanvasBackground = attributes.getColor(R.styleable.CustomView_color_background, Color.WHITE)
        mPaintRectangleBackground.color = attributes.getColor(R.styleable.CustomView_rectangle_color_background, Color.CYAN)
        mPaintRectangleColorTransition = attributes.getColor(R.styleable.CustomView_rectangle_color_transition, Color.RED)
        mStartingRectanglePositionLeftDefault = attributes.getDimensionPixelSize(
            R.styleable.CustomView_rectangle_left_position, RectangleValues.LEFT.position)
        mRectangleColor = attributes.getColor(R.styleable.CustomView_rectangle_color, Color.GREEN)
        mRectangleSizeWidth = attributes.getDimensionPixelSize(R.styleable.CustomView_rectangle_size_width,
            RECTANGLE_SIZE_DEFAULT)
        mPaintRectangle.color = mRectangleColor!!
        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        canvas!!.drawColor(mCanvasBackground!!)

        mRectangle.left = mStartingRectanglePositionLeft!!
        mRectangle.top = mStartingRectanglePositionTop!!
        mRectangle.right = mStartingRectanglePositionRight!!
        mRectangle.bottom = mStartingRectanglePositionBottom!!

        mTextView.setARGB(255, 0, 0, 0)
        mTextView.setTextAlign(Paint.Align.RIGHT);
        mTextView.setTextSize(32f);
        mTextView.setTypeface(Typeface.DEFAULT);

        canvas.drawText("Custom view", 200f, 50f, mTextView)
        canvas.drawRect(mRectangle, mPaintRectangle)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var value = super.onTouchEvent(event)

        when (event!!.action){
            MotionEvent.ACTION_DOWN-> {
                var x: Float = event.x
                var y: Float = event.y

                if(mRectangle.left < x && mRectangle.right > x )
                    if(mRectangle.top < y && mRectangle.bottom > y ) {
                        when(mPaintRectangle.color){
                            mRectangleColor -> mPaintRectangle.color = mPaintRectangleColorTransition!!
                            mPaintRectangle.color -> mPaintRectangle.color = mRectangleColor!!

                        }
                        mStartingRectanglePositionLeft = mStartingRectanglePositionLeftDefault
                        mStartingRectanglePositionRight = mStartingRectanglePositionRightDefault
                        postInvalidate()

                        var rectangleAnimator: ValueAnimator = ValueAnimator.ofFloat(
                            mStartingRectanglePositionRight!!.toFloat(),
                            mRectangleTransitionEndPositionRight!!.toFloat())
                        rectangleAnimator.setDuration(1500)
                        rectangleAnimator.interpolator = BounceInterpolator()

                        rectangleAnimator.addUpdateListener ( object : ValueAnimator.AnimatorUpdateListener {
                            override fun onAnimationUpdate(animation: ValueAnimator?) {
                                mStartingRectanglePositionLeft = mStartingRectanglePositionLeftDefault!! +
                                        (animation?.animatedValue as Float?)?.toInt()!! -
                                        mStartingRectanglePositionRightDefault!!
                                mStartingRectanglePositionRight = (animation?.animatedValue as Float?)?.toInt()!!
                                postInvalidate()
                            }
                        })
                        rectangleAnimator.start()
                        postInvalidate()
                    }
            }
        }
        return value
    }


}