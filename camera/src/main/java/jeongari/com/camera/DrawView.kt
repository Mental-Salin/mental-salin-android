package jeongari.com.camera

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.View

class DrawView : View {

    private var mRatioWidth = 0
    private var mRatioHeight = 0

    private var rectF : RectF ?= null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mRatioX: Float = 0.toFloat()
    private var mRatioY: Float = 0.toFloat()
    private var mImgWidth: Int = 0
    private var mImgHeight: Int = 0

    private var p_all : ArrayList<PointF> = ArrayList<PointF>()

    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = STROKE
            strokeWidth = dip(2).toFloat()
        }
    }
    private val pPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = STROKE
            strokeWidth = dip(3).toFloat()
        }
    }

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    fun setImgSize(
        width: Int,
        height: Int
    ) {
        mImgWidth = width
        mImgHeight = height
        requestLayout()
    }

    fun setDrawPoint(
        rectF: RectF,
        all: ArrayList<PointF>,
        ratio: Float
    ) {
        this.rectF = null
        this.p_all = ArrayList<PointF>()

        val left = rectF.left / ratio / mRatioX
        val right = rectF.right / ratio / mRatioX
        val bottom = rectF.bottom / ratio / mRatioY
        val top = rectF.top / ratio / mRatioY

        for (each in all ){
            val point = PointF(each.x/ ratio / mRatioX, each.y/ ratio / mRatioY )
            this.p_all.add(point)
        }

        this.rectF = RectF(left,top,right,bottom)
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
     * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    fun setAspectRatio(
        width: Int,
        height: Int
    ) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(rectF != null){
            mPaint.color = Color.parseColor("#86AF49")
            canvas.drawRect(rectF,mPaint)
        }
        if (p_all != null){
            pPaint.color = Color.parseColor("#FF9933")
            for (each in p_all!!){
                canvas.drawPoint(each.x, each.y, pPaint)
            }
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                mWidth = width
                mHeight = width * mRatioHeight / mRatioWidth
            } else {
                mWidth = height * mRatioWidth / mRatioHeight
                mHeight = height
            }
        }

        setMeasuredDimension(mWidth, mHeight)

        mRatioX = mImgWidth.toFloat() / mWidth
        mRatioY = mImgHeight.toFloat() / mHeight

    }
}