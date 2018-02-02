package com.vnm.numby.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vnm.numby.R;


/**
 * Created by Vahid Mohammadi
 * Created at 960628
 */


public class Numby extends FrameLayout {
    private static final long THRESHOLD = 1000L;

    private Context context;
    private AttributeSet attrs;
    private int styleAttr;
    private OnClickListener mListener;
    private int initialNumber;
    private int lastNumber;
    private int currentNumber;
    private int finalNumber;

    // throttling features
    private boolean useThrottling;
    private long lastEventTime = 0;
    private Handler handler;
    private Runnable runnable;


    private TextView textView;
    private TextView subButton;
    private TextView addButton;
    private int backgroundColor;
    private CardView container;

    private OnValueChangeListener mOnValueChangeListener;
    private float textSize;
    private int textColor;
    private int addBtnColor;
    private int subBtnColor;
    private boolean isCircular;
    private int cornerRadii;
    private boolean isCollapsible;
    private float alpha;
    private int elevation;
    private Drawable addBtnDrawable;
    private Drawable subBtnDrawable;
    private int typeface;


    public Numby(Context context) {
        super(context);
        this.context = context;
        inflateLayout();
        initAttributes();
        initView();
    }

    public Numby(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        inflateLayout();
        initAttributes();
        initView();
    }

    public Numby(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.styleAttr = defStyleAttr;
        inflateLayout();
        initAttributes();
        initView();
    }

    private void inflateLayout() {
        inflate(context, R.layout.numby_layout, this);
        subButton = findViewById(R.id.subtract_btn);
        addButton = findViewById(R.id.add_btn);
        textView = findViewById(R.id.number_counter);
        container = findViewById(R.id.layout);
    }

    private void initAttributes() {
        final Resources res = getResources();
        final int defaultColor = res.getColor(R.color.colorPrimary);
        final int defaultTextColor = res.getColor(R.color.colorText);

        TypedArray styleable = context.obtainStyledAttributes(attrs, R.styleable.Numby, styleAttr, 0);


        backgroundColor = styleable.getColor(R.styleable.Numby_backgroundColor, defaultColor);
        textColor = styleable.getColor(R.styleable.Numby_textColor, defaultTextColor);
        addBtnColor = styleable.getColor(R.styleable.Numby_addDrawableColor, defaultTextColor);
        subBtnColor = styleable.getColor(R.styleable.Numby_subDrawableColor, defaultTextColor);
        initialNumber = styleable.getInt(R.styleable.Numby_initialNumber, 0);
        finalNumber = styleable.getInt(R.styleable.Numby_finalNumber, Integer.MAX_VALUE);
        textSize = styleable.getDimensionPixelSize(R.styleable.Numby_textSize, 13);
        isCircular = styleable.getBoolean(R.styleable.Numby_isCircularEdge, false);
        cornerRadii = styleable.getDimensionPixelSize(R.styleable.Numby_cornerRadius, dp2Px(context, 3));
        isCollapsible = styleable.getBoolean(R.styleable.Numby_collapsible, true);
        elevation = styleable.getDimensionPixelOffset(R.styleable.Numby_layoutElevation, dp2Px(context, 3));
        addBtnDrawable = styleable.getDrawable(R.styleable.Numby_addDrawable);
        subBtnDrawable = styleable.getDrawable(R.styleable.Numby_subDrawable);
        alpha = styleable.getFloat(R.styleable.Numby_backgroundAlpha, 1);
        useThrottling = styleable.getBoolean(R.styleable.Numby_useThrottling, true);
        typeface = styleable.getResourceId(R.styleable.Numby_fontTypeFace,-1);

        styleable.recycle();

        currentNumber = initialNumber;
        lastNumber = initialNumber;
    }


    private void initView() {
        container.setClipToPadding(false);
        setBackgroundColor(backgroundColor);
        setAlpha(alpha);
        setTypeFace();
        setTextColor(textColor);
        setTextSize(textSize);
        initButtons();

        textView.setText(String.valueOf(initialNumber));

        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                int num = Integer.valueOf(textView.getText().toString());
                setNumber(String.valueOf(num - 1), true);
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                int num = Integer.valueOf(textView.getText().toString());
                setNumber(String.valueOf(num + 1), true);
            }
        });


//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            // Optional: Prevent pre-L from adding inner card padding
//            container.setPreventCornerOverlap(false);
//            container.setUseCompatPadding(true);
//        }
        setElevation(elevation);
        initViewRadius(cornerRadii);

        if (!isCollapsible) {
            textView.setVisibility(VISIBLE);
            subButton.setVisibility(VISIBLE);
        }

        if (useThrottling) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    mOnValueChangeListener.onValueChange(Numby.this, lastNumber, currentNumber);
                }
            };
        }

    }

    private void setTypeFace() {
        if (typeface == -1) return;
        textView.setTypeface(ResourcesCompat.getFont(getContext(),typeface));
        if (addBtnDrawable == null) addButton.setTypeface(ResourcesCompat.getFont(getContext(),typeface));
        if (subBtnDrawable == null) subButton.setTypeface(ResourcesCompat.getFont(getContext(),typeface));
    }

    private void initButtons() {
        addButton.setTextColor(addBtnColor);
        subButton.setTextColor(subBtnColor);
        subButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        addButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        if (addBtnDrawable == null) {
            addButton.setText("+");
        } else {
            Drawable drawable = tintDrawable(addBtnDrawable, addBtnColor);
            addButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        if (subBtnDrawable == null) {
            subButton.setText("-");
        } else {
            Drawable drawable = tintDrawable(subBtnDrawable, subBtnColor);
            subButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    @Override
    public void setElevation(float elevation) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            elevation = 0;
            int padding = dp2Px(context, 4);

            container.setMaxCardElevation(elevation);
            container.setContentPadding(
                    container.getContentPaddingLeft() - padding,
                    container.getContentPaddingTop() - padding,
                    container.getContentPaddingRight() - padding,
                    container.getContentPaddingBottom() - padding);
        }
        container.setCardElevation(elevation);
    }

    private Drawable tintDrawable(Drawable drawable, int color) {
        if (drawable == null) return null;

        Drawable out = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(out, color);
        DrawableCompat.setTintMode(out, PorterDuff.Mode.SRC_IN);
        return out;
    }

    private void initViewRadius(float radii) {
        if (isCircular) {
            container.post(new Runnable() {
                @Override
                public void run() {
                    container.setRadius(container.getHeight() / 2);
                }
            });
        } else {
            container.setRadius(radii);
        }
    }


    private void callListener(View view) {
        if (mListener != null) {
            mListener.onClick(view);
        }

        if (mOnValueChangeListener != null && lastNumber != currentNumber) {
            if (!useThrottling) {
                mOnValueChangeListener.onValueChange(Numby.this,lastNumber,currentNumber);
                return;
            }


            // throttle user interaction and decrease the calls back
            if ( (System.currentTimeMillis() - lastEventTime) <= THRESHOLD) {
                lastEventTime = System.currentTimeMillis();

                // reset timer
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, THRESHOLD);
            } else {
                // start a timer
                handler.postDelayed(runnable, THRESHOLD);
            }
        }
    }

    public String getNumber() {
        return String.valueOf(currentNumber);
    }


    private void setNumber(String number) {
        lastNumber = currentNumber;
        this.currentNumber = Integer.parseInt(number);
        if (this.currentNumber > finalNumber) {
            this.currentNumber = finalNumber;
        }
        if (this.currentNumber < initialNumber) {
            this.currentNumber = initialNumber;
        }
        textView.setText(String.valueOf(currentNumber));
    }


    public void setNumber(String number, boolean notifyListener) {
        setNumber(number);
        if (isCollapsible) handleVisibility();
        if (notifyListener) {
            callListener(this);
        }
    }

    private void handleVisibility() {
        if (currentNumber == initialNumber) {
            subButton.setVisibility(GONE);
            textView.setVisibility(GONE);
        } else {
            if (subButton.getVisibility() != VISIBLE) {
                subButton.setVisibility(VISIBLE);
                textView.setVisibility(VISIBLE);
            }
        }

    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mListener = onClickListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    public void setTextColor(int textColor) {
        textView.setTextColor(textColor);
    }

    public void setTextSize(float textSize) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnValueChangeListener {
        void onValueChange(Numby view, int oldValue, int newValue);
    }

    public void setRange(Integer startingNumber, Integer endingNumber) {
        this.initialNumber = startingNumber;
        this.finalNumber = endingNumber;
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        container.setCardBackgroundColor(color);
    }

    public static int dp2Px(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics());
    }
}
