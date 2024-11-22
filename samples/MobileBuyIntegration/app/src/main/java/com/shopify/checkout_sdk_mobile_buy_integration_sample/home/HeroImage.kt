import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shopify.checkout_sdk_mobile_buy_integration_sample.R

@Composable
fun HeroImage() {
    Surface(
        color = Color.Black, modifier = Modifier
            .fillMaxWidth()
            .alpha(0.75f)
    ) {
        val brightness = -70f
        val contrast = .7f
        val colorMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )

        Image(
            painter = painterResource(id = R.drawable.hero),
            contentDescription = stringResource(id = R.string.hero_content_description),
            modifier = Modifier.wrapContentSize(unbounded = true, align = Alignment.TopCenter),
            colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
        )
    }
}
