package com.parallax.parallaxlauncher.ui.modes.swiss.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

private const val PAPER_AGSL = """
uniform float2 uResolution;

float hash(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

half4 main(float2 fragCoord) {
    half3 paper = half3(0.957, 0.945, 0.918);
    // Fine grain + low-frequency fibre mottling
    float grain = hash(floor(fragCoord));
    float fibre = hash(floor(fragCoord / 6.0) + 17.0);
    paper -= half(grain * 0.04 + fibre * 0.02);
    // Soft edge vignette like aged newsprint
    float2 uv = fragCoord / uResolution;
    float v = uv.x * uv.y * (1.0 - uv.x) * (1.0 - uv.y);
    paper -= half(0.05 * (1.0 - clamp(pow(16.0 * v, 0.2), 0.0, 1.0)));
    return half4(paper, 1.0);
}
"""

val PaperColor = Color(0xFFF4F1EA)

/** Holds either an AGSL shader (API 33+) or precomputed speckle for older devices. */
class PaperGrain {
    private val shader: RuntimeShader? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) create() else null

    private val specks = List(900) {
        val r = Random(it)
        Triple(r.nextFloat(), r.nextFloat(), r.nextFloat())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun create() = RuntimeShader(PAPER_AGSL)

    fun draw(scope: DrawScope) = with(scope) {
        val s = shader
        if (s != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            s.setFloatUniform("uResolution", size.width, size.height)
            drawRect(ShaderBrush(s))
        } else {
            drawRect(PaperColor)
            for ((x, y, a) in specks) {
                drawCircle(
                    Color.Black.copy(alpha = 0.03f + a * 0.05f),
                    0.8f,
                    Offset(x * size.width, y * size.height),
                )
            }
        }
    }
}
