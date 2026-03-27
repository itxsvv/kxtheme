package vibecode.karootheme.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import vibecode.karootheme.service.MapColorStorage;

public class MapCanvasView extends View {
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final float OUTLINE_WIDTH = 2f;
    private static final float LABEL_SHADOW_OFFSET = 2f;
    private static final int ROAD_FILL_COLOR = 0xFFD9D9D9;
    private static final int ROAD_BORDER_COLOR = 0xFF8A8A8A;

    static final int REGION_FOREST = 0;
    static final int REGION_FARM = 1;
    static final int REGION_SCRUB = 2;
    static final int REGION_GRASS = 3;
    static final int REGION_RIVER = 4;

    public interface OnRegionTapListener {
        void onRegionTap(String regionName);
    }

    private final Paint fillPaint = createFillPaint();
    private final Paint strokePaint = createOutlinePaint();
    private final Paint textPaint = createTextPaint();
    private final Paint decorativeRiverPaint = createFillPaint();
    private final Paint decorativeForestPaint = createFillPaint();
    private final Paint decorativeFarmPaint = createFillPaint();
    private final Paint decorativeScrubPaint = createFillPaint();
    private final Paint decorativeGrassPaint = createFillPaint();
    private final Paint roadFillPaint = createRoadPaint(ROAD_FILL_COLOR, 14f);
    private final Paint roadBorderPaint = createRoadPaint(ROAD_BORDER_COLOR, 20f);
    private final Paint thinRoadPaint = createRoadPaint(0xFFE6E0D4, 4f);
    private final RectF forestRect = new RectF();
    private final RectF farmRect = new RectF();
    private final RectF scrubRect = new RectF();
    private final RectF grassRect = new RectF();
    private final RectF riverRect = new RectF();
    private final RectF decorativeMapRect = new RectF();
    private final Path decorativeRiverPath = new Path();
    private final Path decorativeForestPath = new Path();
    private final Path decorativeFarmPath = new Path();
    private final Path decorativeScrubPath = new Path();
    private final Path roadPath = new Path();
    private final Path sideRoadPath = new Path();
    private MapColors mapColors;
    private OnRegionTapListener onRegionTapListener;

    public MapCanvasView(Context context) {
        super(context);
        init();
    }

    public MapCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mapColors = MapColorStorage.ensureAndLoad(getContext());
    }

    public void setOnRegionTapListener(@Nullable OnRegionTapListener listener) {
        onRegionTapListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float stripeWidth = w * 0.30f;
        float stripeHeight = h * 0.20f;
        float right = w;
        float left = right - stripeWidth;

        forestRect.set(left, 0f, right, stripeHeight);
        farmRect.set(left, stripeHeight, right, stripeHeight * 2f);
        scrubRect.set(left, stripeHeight * 2f, right, stripeHeight * 3f);
        grassRect.set(left, stripeHeight * 3f, right, stripeHeight * 4f);
        riverRect.set(left, stripeHeight * 4f, right, stripeHeight * 5f);

        decorativeMapRect.set(0f, 0f, left, h);

        decorativeRiverPath.reset();
        decorativeRiverPath.moveTo(0f, h * 0.20f);
        decorativeRiverPath.lineTo(left * 0.14f, h * 0.24f);
        decorativeRiverPath.lineTo(left * 0.28f, h * 0.40f);
        decorativeRiverPath.lineTo(left * 0.44f, h * 0.63f);
        decorativeRiverPath.lineTo(left * 0.56f, h * 0.86f);
        decorativeRiverPath.lineTo(left * 0.60f, h);
        decorativeRiverPath.lineTo(0f, h);
        decorativeRiverPath.close();

        decorativeFarmPath.reset();
        decorativeFarmPath.moveTo(0f, h * 0.10f);
        decorativeFarmPath.lineTo(left * 0.12f, h * 0.12f);
        decorativeFarmPath.lineTo(left * 0.34f, h * 0.34f);
        decorativeFarmPath.lineTo(left * 0.58f, h * 0.52f);
        decorativeFarmPath.lineTo(left * 0.82f, h * 0.70f);
        decorativeFarmPath.lineTo(left, h * 0.84f);
        decorativeFarmPath.lineTo(left, h);
        decorativeFarmPath.lineTo(left * 0.52f, h);
        decorativeFarmPath.lineTo(left * 0.40f, h * 0.84f);
        decorativeFarmPath.lineTo(left * 0.24f, h * 0.58f);
        decorativeFarmPath.lineTo(left * 0.10f, h * 0.34f);
        decorativeFarmPath.lineTo(0f, h * 0.24f);
        decorativeFarmPath.close();

        decorativeScrubPath.reset();
        decorativeScrubPath.moveTo(left * 0.42f, h * 0.18f);
        decorativeScrubPath.lineTo(left * 0.62f, h * 0.24f);
        decorativeScrubPath.lineTo(left * 0.76f, h * 0.34f);
        decorativeScrubPath.lineTo(left * 0.70f, h * 0.46f);
        decorativeScrubPath.lineTo(left * 0.50f, h * 0.44f);
        decorativeScrubPath.lineTo(left * 0.36f, h * 0.30f);
        decorativeScrubPath.close();

        decorativeForestPath.reset();
        decorativeForestPath.moveTo(left * 0.04f, h * 0.08f);
        decorativeForestPath.lineTo(left * 0.20f, h * 0.10f);
        decorativeForestPath.lineTo(left * 0.28f, h * 0.22f);
        decorativeForestPath.lineTo(left * 0.14f, h * 0.26f);
        decorativeForestPath.lineTo(0f, h * 0.20f);
        decorativeForestPath.close();

        roadPath.reset();
        roadPath.moveTo(left * 0.02f, h * 0.08f);
        roadPath.lineTo(left * 0.18f, h * 0.18f);
        roadPath.lineTo(left * 0.28f, h * 0.32f);
        roadPath.lineTo(left * 0.36f, h * 0.40f);
        roadPath.lineTo(left * 0.58f, h * 0.46f);
        roadPath.lineTo(left * 0.78f, h * 0.60f);
        roadPath.lineTo(left * 0.96f, h * 0.80f);

        sideRoadPath.reset();
        sideRoadPath.moveTo(left * 0.48f, h * 0.40f);
        sideRoadPath.lineTo(left * 0.70f, h * 0.34f);
        sideRoadPath.lineTo(left * 0.92f, h * 0.22f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        decorativeGrassPaint.setColor(mapColors.getFarmColor());
        decorativeFarmPaint.setColor(mapColors.getForestColor());
        decorativeScrubPaint.setColor(mapColors.getScrubColor());
        decorativeForestPaint.setColor(mapColors.getGrassColor());
        decorativeRiverPaint.setColor(mapColors.getRiverColor());

        canvas.drawRect(decorativeMapRect, decorativeGrassPaint);
        canvas.drawPath(decorativeFarmPath, decorativeFarmPaint);
        canvas.drawPath(decorativeScrubPath, decorativeScrubPaint);
        canvas.drawPath(decorativeForestPath, decorativeForestPaint);
        canvas.drawPath(decorativeRiverPath, decorativeRiverPaint);
        canvas.drawPath(roadPath, roadBorderPaint);
        canvas.drawPath(roadPath, roadFillPaint);
        canvas.drawPath(sideRoadPath, thinRoadPaint);

        fillPaint.setColor(mapColors.getForestColor());
        canvas.drawRect(forestRect, fillPaint);
        canvas.drawRect(forestRect, strokePaint);
        drawCenteredLabel(canvas, getContext().getString(R.string.forest_name), forestRect);

        fillPaint.setColor(mapColors.getFarmColor());
        canvas.drawRect(farmRect, fillPaint);
        canvas.drawRect(farmRect, strokePaint);
        drawCenteredLabel(canvas, getContext().getString(R.string.farm_name), farmRect);

        fillPaint.setColor(mapColors.getScrubColor());
        canvas.drawRect(scrubRect, fillPaint);
        canvas.drawRect(scrubRect, strokePaint);
        drawCenteredLabel(canvas, getContext().getString(R.string.scrub_name), scrubRect);

        fillPaint.setColor(mapColors.getGrassColor());
        canvas.drawRect(grassRect, fillPaint);
        canvas.drawRect(grassRect, strokePaint);
        drawCenteredLabel(canvas, getContext().getString(R.string.grass_name), grassRect);

        fillPaint.setColor(mapColors.getRiverColor());
        canvas.drawRect(riverRect, fillPaint);
        canvas.drawRect(riverRect, strokePaint);
        drawCenteredLabel(canvas, getContext().getString(R.string.river_name), riverRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int regionIndex = getRegionIndex(event.getX(), event.getY());
            if (regionIndex == -1) {
                return false;
            }
            String regionName = getRegionName(regionIndex);
            showColorPicker(regionIndex);
            if (onRegionTapListener != null) {
                onRegionTapListener.onRegionTap(regionName);
            }
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int getRegionIndex(float x, float y) {
        if (riverRect.contains(x, y)) {
            return REGION_RIVER;
        }

        if (grassRect.contains(x, y)) {
            return REGION_GRASS;
        }

        if (scrubRect.contains(x, y)) {
            return REGION_SCRUB;
        }

        if (farmRect.contains(x, y)) {
            return REGION_FARM;
        }

        if (forestRect.contains(x, y)) {
            return REGION_FOREST;
        }

        return -1;
    }

    private String getRegionName(int regionIndex) {
        if (regionIndex == REGION_RIVER) {
            return getContext().getString(R.string.river_name);
        }
        if (regionIndex == REGION_GRASS) {
            return getContext().getString(R.string.grass_name);
        }
        if (regionIndex == REGION_SCRUB) {
            return getContext().getString(R.string.scrub_name);
        }
        if (regionIndex == REGION_FARM) {
            return getContext().getString(R.string.farm_name);
        }
        return getContext().getString(R.string.forest_name);
    }

    private void showColorPicker(int regionIndex) {
        final int initialColor = getRegionColor(regionIndex);
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_picker, null);
        final ColorPickerView colorPickerView = dialogView.findViewById(R.id.dialog_color_picker_view);
        final BrightnessSlideBar brightnessSlideBar = dialogView.findViewById(R.id.dialog_brightness_slider);
        final int[] selectedColor = {initialColor};

        colorPickerView.attachBrightnessSlider(brightnessSlideBar);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                selectedColor[0] = envelope.getColor();
            }
        });

        try {
            colorPickerView.selectByHsvColor(initialColor);
        } catch (IllegalAccessException ignored) {
            colorPickerView.setInitialColor(initialColor);
        }

        new AlertDialog.Builder(getContext())
                .setTitle(getRegionName(regionIndex))
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    setRegionColor(regionIndex, selectedColor[0]);
                    MapColorStorage.writeColors(getContext(), mapColors);
                    invalidate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int getRegionColor(int regionIndex) {
        if (regionIndex == REGION_RIVER) {
            return mapColors.getRiverColor();
        }
        if (regionIndex == REGION_GRASS) {
            return mapColors.getGrassColor();
        }
        if (regionIndex == REGION_SCRUB) {
            return mapColors.getScrubColor();
        }
        if (regionIndex == REGION_FARM) {
            return mapColors.getFarmColor();
        }
        return mapColors.getForestColor();
    }

    private void setRegionColor(int regionIndex, int color) {
        if (regionIndex == REGION_RIVER) {
            mapColors.setRiverColor(color);
            return;
        }
        if (regionIndex == REGION_GRASS) {
            mapColors.setGrassColor(color);
            return;
        }
        if (regionIndex == REGION_SCRUB) {
            mapColors.setScrubColor(color);
            return;
        }
        if (regionIndex == REGION_FARM) {
            mapColors.setFarmColor(color);
            return;
        }
        mapColors.setForestColor(color);
    }

    private Paint createFillPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private Paint createOutlinePaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(OUTLINE_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(OUTLINE_WIDTH);
        return paint;
    }

    private Paint createTextPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(32f);
        return paint;
    }

    private Paint createRoadPaint(int color, float width) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }

    private void drawCenteredLabel(Canvas canvas, String text, RectF rect) {
        float baselineY = rect.centerY() - ((textPaint.ascent() + textPaint.descent()) / 2f);
        drawDualColorText(canvas, text, rect.centerX(), baselineY);
    }

    private void drawDualColorText(Canvas canvas, String text, float x, float y) {
        textPaint.setColor(OUTLINE_COLOR);
        canvas.drawText(text, x + LABEL_SHADOW_OFFSET, y + LABEL_SHADOW_OFFSET, textPaint);
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText(text, x, y, textPaint);
    }
}
