# CustomView
自定义view
先来看一下效果图吧
![2018-01-31-20-24-45.gif](http://upload-images.jianshu.io/upload_images/5128077-326e2b1f3e832463.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
我们在自定义一个控件的时候首先要分析它的结构。
可以看出一共有四部分四个圆弧，两个线条圆弧，两个点状圆弧绘画的步骤分别是
#####0-360度
1.从0点开始，角度从360度到0度的一个线条圆
2.一小段一小段的弧线，然后让画布旋转绘制成圆弧。
#####360-720度
3.从0点开始角度从0-360度的一个线条圆
4.一小段一小段的弧线，然后让画布旋转绘制成圆弧。

####首先我们自定义一个控件，需要我们先分析它的属性，然后定义自定义属性：
-  mStartColor    开始的颜色（因为是渐变样式）
- mEndColor 结束的颜色（因为是渐变样式）
-  mArcRadian 一小段弧线的弧度  
-  mArcWidth 画弧线的宽度 
- mArcSpacing 每一段弧线的距离
在values下创建attrs.xml文件
```
<resources>
    <declare-styleable name="LoradingHeard">
        <attr name="circleStartColor" format="color|reference"/>
        <attr name="circleEndColor" format="color|reference"/>
        <attr name="circleArcWidth" format="integer|reference"/>
        <attr name="circleArcRadian" format="integer|reference"/>
        <attr name="circleArcSpacing" format="integer|reference"/>
    </declare-styleable>
</resources>
```
declare-styleable： 表示一个属性组。它的name必须和你自定义view的名字相同。
获取自定义属性的方法我们可以通过
getContext().obtainStyledAttributes()获取TypedArray，
通过TypedArray来获取自定义属性的值，获取完成之后，我们必须要用typedArray 的recycle（）的方法将 TypedArray 回收。
```
mStartColor = typedArray.getColor(R.styleable.LoradingHeard_circleStartColor,
               ContextCompat.getColor(context, R.color.colorPrimary));

mEndColor = typedArray.getColor(R.styleable.LoradingHeard_circleEndColor,
               ContextCompat.getColor(context, R.color.colorPrimaryDark));

mArcSpacing = typedArray.getInteger(R.styleable.LoradingHeard_circleArcSpacing, 10);

mArcRadian = typedArray.getInteger(R.styleable.LoradingHeard_circleArcRadian, 5);

mArcWidth = typedArray.getInteger(R.styleable.LoradingHeard_circleArcWidth, 5);
 typedArray.recycle();
```
我们可以看到在获取玩xml的自定义属性以后调用了recycle（），这个方法，那么为什么要调用这个方法？
首先我们来看一下typedArray的创建

```
 @NonNull
        TypedArray obtainStyledAttributes(@NonNull Resources.Theme wrapper,
                AttributeSet set,
                @StyleableRes int[] attrs,
                @AttrRes int defStyleAttr,
                @StyleRes int defStyleRes) {
            synchronized (mKey) {
                final int len = attrs.length;
                final TypedArray array = TypedArray.obtain(wrapper.getResources(), len);
                 其他代码
                return array;
            }
        }
```
可以看出来并不是实例化出来的，是调用了TypedArray的静态方法来获得typedArray的实例的。但是到这里还是没能解决我们的问题，那就接着往下看TypedArray.obtain的方法：
```
   static TypedArray obtain(Resources res, int len) {
        TypedArray attrs = res.mTypedArrayPool.acquire();
        if (attrs == null) {
            attrs = new TypedArray(res);
        }

        attrs.mRecycled = false;
        // Reset the assets, which may have changed due to configuration changes
        // or further resource loading.
        attrs.mAssets = res.getAssets();
        attrs.mMetrics = res.getDisplayMetrics();
        attrs.resize(len);
        return attrs;
    }
```
我们发现内部是一个单利的形式，发现typedArray这个实例是从线程池里面获取的。这样我们是不是就知道了，其实程序在运行时维护了一个 TypedArray的池，需要的时候我们会向线程池请求实例，用完后调用recycle（）方法释放这个实例，从而让其可以被复用，而不是重新创建，这样就减少了系统频繁创建array，导致oom。

###Shader
从效果图中可以看出来整个线条圆是分为渐变的颜色。
如果想画出渐变的效果我们可以用这个类 Shader，它有五个子类

 - BitmapShader ： 位图绘制时纹理的着色器
 - ComposeShader ：组合着色器
- LinearGradient ：线性渐变着色器（我们要用到这个）
- RadialGradient ： 径向渐变着色器
- SweepGradient ：绕着一个中心点进行扫描的渐变着色器
一般用再paint.setShader(shader)中，shader是一个Shader对象。
#####LiearGradient
LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile)
- x0表示渐变的起始点x坐标
- y0表示渐变的起始点y坐标
- x1表示渐变的终点x坐标
- y1表示渐变的终点y坐标
- color0表示渐变开始颜色
- color1表示渐变结束颜色
- tile表示平铺模式
```
 Shader shader = new LinearGradient(0f, 0f, mWidth, mHeight,
                mStartColor, mEndColor, Shader.TileMode.CLAMP);
paint.setShader(shader);
```
#####现在我们的所有准备活动都准备完毕，那就开始画吧上核心代码
```
A:第一个圆弧从0点开始，角度从360度到0度
  if (maxAngle <= 360) {
            float angle = 0;
            canvas.rotate(mRatio * maxAngle / 360, mWidth / 2, mHeight / 2);
            canvas.drawArc(mRectF, maxAngle, 360 - maxAngle, false, paint);
B:一小段弧线，然后让画布旋转360度
            while (angle <= maxAngle) {
                float length = mArcRadian * angle / maxAngle;
                canvas.drawArc(mRectF, 0, length, false, paint);
                canvas.rotate(mArcSpacing, mWidth / 2, mHeight / 2);
                angle += mArcSpacing;
            }
C::第一个圆弧从0点开始，角度从度到0度360度。
        } else {
            float angle = 0;
            canvas.rotate(mRatio + mRatio * (maxAngle - 360) / 360, mWidth / 2, mHeight / 2);
            canvas.drawArc(mRectF, 0, maxAngle - 360, false, paint);
            canvas.rotate(maxAngle - 360, mWidth / 2, mHeight / 2);
D:一小段弧线，然后让画布旋转360度
            while (angle <= 720 - maxAngle) {
                float length = mArcRadian * angle / (720 - maxAngle);
                canvas.drawArc(mRectF, 0, length, false, paint);
                canvas.rotate(mArcSpacing, mWidth / 2, mHeight / 2);
                angle += mArcSpacing;
            }
        }
E:刷新数据进行更新绘制
        if (maxAngle <= 720) {
            maxAngle += mArcSpacing;
            postInvalidateDelayed(30);
        }
```
