package com.parallax.parallaxlauncher.ui.modes.cyberdeck.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

private const val CRT_AGSL = """
uniform float2 uResolution;
uniform shader uContent;

float2 curve(float2 uv) {
    uv = (uv - 0.5) * 2.0;
    uv *= 1.04;
    uv.x *= 1.0 + pow((abs(uv.y) / 6.0), 2.0);
    uv.y *= 1.0 + pow((abs(uv.x) / 5.0), 2.0);
    return (uv / 2.0) + 0.5;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;
    float2 c = curve(uv);
    if (c.x < 0.0 || c.x > 1.0 || c.y < 0.0 || c.y > 1.0) return half4(0.0, 0.0, 0.0, 1.0);

    // Chromatic aberration
    float2 px = float2(1.5, 0.0);
    half r = uContent.eval(c * uResolution - px).r;
    half4 mid = uContent.eval(c * uResolution);
    half b = uContent.eval(c * uResolution + px).b;
    half3 col = half3(r, mid.g, b);

    // Phosphor bloom (cheap): add a blurred-ish neighbour tap
    half3 glow = uContent.eval(c * uResolution + float2(0.0, 2.0)).rgb * 0.15;
    col += glow;

    // Scanlines
    col -= half(0.07 * (0.5 + 0.5 * sin(c.y * uResolution.y * 2.2)));
    // Vignette
    float v = c.x * c.y * (1.0 - c.x) * (1.0 - c.y);
    col *= half(clamp(pow(16.0 * v, 0.2), 0.0, 1.0));
    return half4(col, 1.0);
}
"""

/** AGSL CRT post-process via RenderEffect on API 33+, with fallback overlay on older devices. */
class CrtShader {
    private val shader: RuntimeShader? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) create() else null

    val supported get() = shader != null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun create() = RuntimeShader(CRT_AGSL)

    fun apply(scope: GraphicsLayerScope) {
        val s = shader ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        s.setFloatUniform("uResolution", scope.size.width, scope.size.height)
        scope.renderEffect = RenderEffect.createRuntimeShaderEffect(s, "uContent").asComposeRenderEffect()
    }

    /** Fallback scanline raster for devices below API 33 */
    fun drawFallbackScanlines(scope: DrawScope) = with(scope) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return@with
        val step = 4f
        var y = 0f
        val scanlineColor = Color.Black.copy(alpha = 0.18f)
        while (y < size.height) {
            drawLine(
                scanlineColor,
                Offset(0f, y),
                Offset(size.width, y),
                strokeWidth = 1.5f,
            )
            y += step
        }
    }
}
