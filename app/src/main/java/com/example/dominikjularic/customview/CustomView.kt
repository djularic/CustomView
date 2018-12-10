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

/**
 * Custom view that is used to define a rectangle that covers max half of specified view
 *  and uses animation that translates rectangle over horizontal axis to the other half of view.
 */
class CustomView : View {


    /**
     * Definition of values used in the view
     * default size of rectangle is used, to preset the size before scaling
     */
    private var RECTANGLE_SIZE_DEFAULT : Int = 200
    private var mCanvasBackground: Int? = null
    private lateinit var mRectangleBackground: Rect
    private lateinit var mPaintRectangleBackground: Paint
    /**
     * Definition of values used to define rectangle
     * Default positions are used so we can restart the position of rectangle to starting position while rectangle
     * is in transition. Starting positions define the starting points that define rectangle, which are updated with
     * transition, translation on horizontal axis
     */
    private var mStartingRectanglePositionTop: Int? = null
    private var mStartingRectanglePositionLeft: Int? = null
    private var mStartingRectanglePositionRight: Int? = null
    private var mStartingRectanglePositionBottom: Int? = null
    private var mStartingRectanglePositionLeftDefault: Int? = null
    private var mStartingRectanglePositionRightDefault: Int? = null
    private var mRectangleTransitionEndPositionRight: Int? = null
    /**
     * Objects that are used to draw shape ( rectangle) , and fill them with color
     */
    private lateinit var mTextView: Paint
    private lateinit var mRectangle: Rect
    private lateinit var mPaintRectangle: Paint
    /**
     * Custom color, that can be defined in xml for color used for transition
     */
    private var mPaintRectangleColorTransition: Int? = null
    /**
     * Width and Height that are used in the XML file by user
     */
    private var mCustomViewHeight: Int? = null
    private var mCustomViewWidth: Int? = null

    /**
     * Starting values of rectangle, used to preset the values of rectangle before scaling the value based on
     * view size, we can't set the scaling value as first value, because we get that value with listener that
     * is updated when the onMeasure function is done
     */
    enum class RectangleValues(val position: Int){
        TOP(0),
        LEFT(0),
    }

    /**
     * Scaling values of rectangle. These scale values are used to scale the size of rectangle based on the size
     * of width and height specified in the xml layout
     * 0.05 -> 5% value of value defined in layout xml for top and left coordinate 45% for right coordinate and 0.95%
     * for bottom coordinate
     *
     */
    enum class RectangleScale(val factor: Float){
        TOP(0.05f),
        LEFT(0.05f),
        RIGHT(0.45f),
        BOTTOM(0.95f),
        MAX_RECTANGLE_SIZE(0.40f),
        END_POSITION_RIGHT(0.95f)
    }

    /**
     * Custom color that can be set in XML, and put up in Paint object, to change the rectangle color
     */
    private var mRectangleColor: Int? = null
    private var mRectangleSizeWidth: Int? = null

    /**
     * Constructors
     */
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

    /**
     * function to initialize elements in constructors,
     * Argument is attribute set defined in values/attrs.xml file which defines the atributes that can be custom set in
     * layout file
     */

    fun init(set: AttributeSet?){

        /**
         * initialization of objects used to draw shape
         * ANTI_ALIAS_FLAG used to smooth the pixelization
         */
        mTextView = Paint()
        mRectangleBackground = Rect()
        mPaintRectangleBackground = Paint(Paint.ANTI_ALIAS_FLAG)
        mRectangle = Rect()
        mPaintRectangle= Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * if no set is defined in values/attrs.xml, stop with initialization
         */
        if (set == null)
            return

        /**
         * we get the width and height of view defined in xml with viewTreeObserver. viewTreeObserver listens when
         * changes are made in view tree hierarchy, after the onMeasure function, and updates us with that information
         *
         */

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                else
                    viewTreeObserver.removeGlobalOnLayoutListener(this)

                /**
                 * when observer gets the changed data, we initialze our view size with data defined in layout xml
                 * and scale the size of rectangle based on that data
                 */
                mCustomViewHeight = height
                mCustomViewWidth = width
                mStartingRectanglePositionTop = (height*RectangleScale.TOP.factor).toInt()
                mStartingRectanglePositionLeftDefault = (width*RectangleScale.LEFT.factor).toInt()
                mStartingRectanglePositionLeft = (width*RectangleScale.LEFT.factor).toInt()
                mStartingRectanglePositionBottom = (height*RectangleScale.BOTTOM.factor).toInt()

                /**
                 * User can define custom width of rectangle, that can be bigger than half of our view,
                 * and even bigger than the whole view, we scale it down to the maxium size so that our animation works
                 */
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

        /**
         * getting the attributes of our view from the layout xml
         */

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
        /**
         * memory used by TypedArray can be reused by later caller
         */
        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        /**
         * set the background color of canvas
         */
        canvas!!.drawColor(mCanvasBackground!!)

        /**
         * set the coordinates of rectangle
         */

        mRectangle.left = mStartingRectanglePositionLeft!!
        mRectangle.top = mStartingRectanglePositionTop!!
        mRectangle.right = mStartingRectanglePositionRight!!
        mRectangle.bottom = mStartingRectanglePositionBottom!!

        /**
         * set the text attributes
         */
        mTextView.setARGB(255, 0, 0, 0)
        mTextView.setTextAlign(Paint.Align.RIGHT);
        mTextView.setTextSize(32f);
        mTextView.setTypeface(Typeface.DEFAULT);

        /**
         * draw the objects on canvas
         */

        canvas.drawText("Custom view", 200f, 50f, mTextView)
        canvas.drawRect(mRectangle, mPaintRectangle)
    }

    /**
     * function used to respond on gestures made on canvas
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        /**
         * gesture/event that is made on canavas, depending on value we can implement different behaviour
         */
        var value = super.onTouchEvent(event)

        when (event!!.action){
            /**
             * on Click event/gesture
             */
            MotionEvent.ACTION_DOWN-> {
                var x: Float = event.x
                var y: Float = event.y

                /**
                 * we compare the x and y coordinates of click event if they are in the surface/ boundaries of
                 * of our rectangle. If they are we do the animation
                 */
                if(mRectangle.left < x && mRectangle.right > x )
                    if(mRectangle.top < y && mRectangle.bottom > y ) {
                        when(mPaintRectangle.color){
                            /**
                             * on click we change the default color with color for transition
                             */
                            mRectangleColor -> mPaintRectangle.color = mPaintRectangleColorTransition!!
                            mPaintRectangle.color -> mPaintRectangle.color = mRectangleColor!!

                        }
                        /**
                         * if the click is made inside our rectangle in transition we return it to the
                         * default position
                         */
                        mStartingRectanglePositionLeft = mStartingRectanglePositionLeftDefault
                        mStartingRectanglePositionRight = mStartingRectanglePositionRightDefault
                        postInvalidate()

                        /**
                         * Value animator definition that helps us use standard animations with interpolators
                         * we define here the starting and ending position of our object
                         */
                        var rectangleAnimator: ValueAnimator = ValueAnimator.ofFloat(
                            mStartingRectanglePositionRight!!.toFloat(),
                            mRectangleTransitionEndPositionRight!!.toFloat())
                        rectangleAnimator.setDuration(1500)
                        rectangleAnimator.interpolator = BounceInterpolator()

                        /**
                         * we listen to changes on value animator and update our current object on change
                         * only left and right coordinates are changing
                         */
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