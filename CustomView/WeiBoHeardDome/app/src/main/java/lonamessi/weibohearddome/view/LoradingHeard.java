package lonamessi.weibohearddome.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import lonamessi.weibohearddome.R;

/**
 * Created by gyp on 2018/1/30.
 */

public class LoradingHeard extends View {

    private int mStartColor;
    private int mEndColor;
    private int mArcSpacing;
    private int mArcRadian;
    private int mArcWidth;
    private int mWidth;
    private int mHeight;
    private RectF mRectF;
    private Paint paint;
    private int mRatio = 60;
    private float maxAngle = 0;

    public LoradingHeard(Context context) {
        this(context,null);
    }

    public LoradingHeard(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoradingHeard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context,attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (maxAngle <= 360) {
            float angle = 0;
            canvas.rotate(mRatio * maxAngle / 360, mWidth / 2, mHeight / 2);
            canvas.drawArc(mRectF, maxAngle, 360 - maxAngle, false, paint);
            while (angle <= maxAngle) {
                float length = mArcRadian * angle / maxAngle;
                canvas.drawArc(mRectF, 0, length, false, paint);
                canvas.rotate(mArcSpacing, mWidth / 2, mHeight / 2);
                angle += mArcSpacing;
            }
        } else {
            float angle = 0;
            canvas.rotate(mRatio + mRatio * (maxAngle - 360) / 360, mWidth / 2, mHeight / 2);
            canvas.drawArc(mRectF, 0, maxAngle - 360, false, paint);
            canvas.rotate(maxAngle - 360, mWidth / 2, mHeight / 2);
            while (angle <= 720 - maxAngle) {
                float length = mArcRadian * angle / (720 - maxAngle);
                canvas.drawArc(mRectF, 0, length, false, paint);
                canvas.rotate(mArcSpacing, mWidth / 2, mHeight / 2);
                angle += mArcSpacing;
            }
        }

        if (maxAngle <= 720) {
            maxAngle += mArcSpacing;
            postInvalidateDelayed(30);
        }else {
            maxAngle = 0;
            postInvalidateDelayed(30);
        }


    }
   private void initAttrs(Context context,AttributeSet attrs){
       TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoradingHeard);
       mStartColor = typedArray.getColor(R.styleable.LoradingHeard_circleStartColor,
               ContextCompat.getColor(context, R.color.colorPrimary));
       mEndColor = typedArray.getColor(R.styleable.LoradingHeard_circleEndColor,
               ContextCompat.getColor(context, R.color.colorPrimaryDark));
       mArcSpacing = typedArray.getInteger(R.styleable.LoradingHeard_circleArcSpacing, 10);
       mArcRadian = typedArray.getInteger(R.styleable.LoradingHeard_circleArcRadian, 5);
       mArcWidth = typedArray.getInteger(R.styleable.LoradingHeard_circleArcWidth, 5);
       typedArray.recycle();
       paint = new Paint();
       paint.setAntiAlias(true);
       paint.setStyle(Paint.Style.STROKE);
       paint.setStrokeWidth(mArcWidth);
       paint.setStrokeCap(Paint.Cap.ROUND);

   }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
        mHeight = getHeight();
        mRectF = new RectF(mArcWidth, mArcWidth, mWidth - mArcWidth, mHeight - mArcWidth);
        Shader shader = new LinearGradient(0f, 0f, mWidth, mHeight,
                mStartColor, mEndColor, Shader.TileMode.CLAMP);
        paint.setShader(shader);


    }
}
