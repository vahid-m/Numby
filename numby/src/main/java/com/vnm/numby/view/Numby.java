package com.vnm.numby.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.vnm.numby.R;


/**
 * Created by Vahid Mohammadi
 * Created at 960628
 */


public class Numby extends FrameLayout {
    private static final long THRESHOLD = 1000L;

    // views
    private CardView container;
    private TextSwitcher textView;
    private TextView subButton;
    private TextView addButton;


    private int value;
    private int lastValue;
    private int minValue;
    private int maxValue;

    // interfaces
    private OnClickListener onClickListener;
    private OnValueChangeListener onValueChangeListener;


    // throttling features
    private boolean useThrottling;
    private long lastEventTime = 0;
    private Handler handler;
    private Runnable runnable;


    // view attributes
    private int backgroundColor;
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

    private Animation inFromTop;
    private Animation outFromBottom;
    private Animation inFromBottom;
    private Animation outFromTop;

    public Numby(Context context) {
        super(context);
        initAttributes(context, null, 0);
        initView();
    }

    public Numby(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs, 0);
        initView();
    }

    public Numby(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs, defStyleAttr);
        initView();
    }


    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        final Resources res = getResources();
        final int defaultColor = res.getColor(R.color.colorPrimary);
        final int defaultTextColor = res.getColor(R.color.colorText);

        TypedArray styleable = getContext().obtainStyledAttributes(attrs, R.styleable.Numby, defStyleAttr, 0);


        backgroundColor = styleable.getColor(R.styleable.Numby_backgroundColor, defaultColor);
        textColor = styleable.getColor(R.styleable.Numby_textColor, defaultTextColor);
        addBtnColor = styleable.getColor(R.styleable.Numby_addDrawableColor, defaultTextColor);
        subBtnColor = styleable.getColor(R.styleable.Numby_subDrawableColor, defaultTextColor);
        minValue = styleable.getInt(R.styleable.Numby_initialNumber, 0);
        maxValue = styleable.getInt(R.styleable.Numby_finalNumber, Integer.MAX_VALUE);
        textSize = styleable.getDimensionPixelSize(R.styleable.Numby_textSize, 13);
        isCircular = styleable.getBoolean(R.styleable.Numby_isCircularEdge, false);
        cornerRadii = styleable.getDimensionPixelSize(R.styleable.Numby_cornerRadius, dp2Px(context, 3));
        isCollapsible = styleable.getBoolean(R.styleable.Numby_collapsible, true);
        elevation = styleable.getDimensionPixelOffset(R.styleable.Numby_layoutElevation, dp2Px(context, 3));
        addBtnDrawable = styleable.getDrawable(R.styleable.Numby_addDrawable);
        subBtnDrawable = styleable.getDrawable(R.styleable.Numby_subDrawable);
        alpha = styleable.getFloat(R.styleable.Numby_backgroundAlpha, 1);
        useThrottling = styleable.getBoolean(R.styleable.Numby_useThrottling, true);

        styleable.recycle();

        value = minValue;
        lastValue = value;
    }


    private void initView() {
        inflate(getContext(), R.layout.numby_layout, this);

        // setup container
        container = findViewById(R.id.layout);
        container.setClipToPadding(false);
        setBackgroundColor(backgroundColor);
        setAlpha(alpha);
        setElevation(elevation);
        initViewRadius(cornerRadii);


        // setup buttons
        initButtons();

        // setup text
        textView  = findViewById(R.id.number_counter);
        textView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(new ContextThemeWrapper(getContext(), R.style.AppTheme), null, 0);
                myText.setGravity(Gravity.CENTER);
                myText.setTextColor(textColor);
                myText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                myText.setEms(String.valueOf(maxValue).length()-1);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                myText.setLayoutParams(params);

                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        inFromTop = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_top);
        outFromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        inFromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        outFromTop = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_top);


//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            // Optional: Prevent pre-L from adding inner card padding
//            container.setPreventCornerOverlap(false);
//            container.setUseCompatPadding(true);
//        }

        if (useThrottling) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    onValueChangeListener.onValueChange(Numby.this, lastValue, value);
                    lastValue = value;
                }
            };
        }
        setNumber(value , false);
    }

    private void initButtons() {
        subButton = findViewById(R.id.subtract_btn);
        addButton = findViewById(R.id.add_btn);

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


        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                int num = value - 1;
                setNumber(num, true);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                int num = value + 1;
                setNumber(num, true);
            }
        });
    }

    @Override
    public void setElevation(float elevation) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            elevation = 0;
            int padding = dp2Px(getContext(), 4);

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


    private void callListener() {
        if (onClickListener != null)
            onClickListener.onClick(Numby.this);
        if (onValueChangeListener !=null) {
            onValueChangeListener.onValueChange(Numby.this,lastValue, value);
            lastValue = value;
        }

    }

    public String getNumber() {
        return String.valueOf(value);
    }

    public void setNumber(int number, boolean notifyListener) {
        // set the animation type of textSwitcher
        textView.setInAnimation((number > value) ? inFromTop : inFromBottom);
        textView.setOutAnimation((number > value) ? outFromBottom : outFromTop);

        // update the view first
        number = (number > maxValue) ? maxValue : number;
        number = (number < minValue) ? minValue : number;

        if (value == number) return; // it reaches to minimum or maximum value

        // update the text view and current value
        textView.setText(String.valueOf(number));
        value = number;
        if (isCollapsible) handleVisibility();


        // handle listeners
        if (!notifyListener) return;

        if (!useThrottling) {
            callListener();
        } else {
            if (lastValue == number) return;

            // throttle user interaction and decrease the calls back
            if (SystemClock.uptimeMillis() - lastEventTime <= THRESHOLD) {
                handler.removeCallbacks(runnable);
            }
            lastEventTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, THRESHOLD);
        }
    }

    private void handleVisibility() {
        if (value == minValue) {
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
        this.onClickListener = onClickListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    public interface OnClickListener {
        void onClick(Numby view);
    }

    public interface OnValueChangeListener {
        void onValueChange(Numby view, int oldValue, int newValue);
    }

    public void setRange(Integer startingNumber, Integer endingNumber) {
        this.minValue = startingNumber;
        this.maxValue = endingNumber;
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
