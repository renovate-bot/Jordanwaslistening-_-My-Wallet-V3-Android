package com.blockchain.componentlib.button

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import com.blockchain.componentlib.button.common.BaseButtonView

class SecondaryButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseButtonView(context, attrs, defStyleAttr) {

    @Composable
    override fun Content() {
        SecondaryButton(
            onClick = onClick,
            text = text,
            state = buttonState
        )
    }
}

class SmallSecondaryButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseButtonView(context, attrs, defStyleAttr) {

    @Composable
    override fun Content() {
        SecondarySmallButton(
            onClick = onClick,
            text = text,
            state = buttonState
        )
    }
}
