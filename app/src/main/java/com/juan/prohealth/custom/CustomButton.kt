package com.juan.prohealth.custom

// https://www.vogella.com/tutorials/AndroidCustomViews/article.html
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.juan.prohealth.R
import com.juan.prohealth.ui.common.alignTo
import com.juan.prohealth.databinding.CustomButtonBinding

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {

        val binding = CustomButtonBinding.inflate(LayoutInflater.from(context), this)

        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.custom_button_attrs, 0, 0)
            val title = resources.getText(
                typedArray.getResourceId(
                    R.styleable.custom_button_attrs_cb_title,
                    R.string.empty
                )
            )
            binding.menuText.text = title
            binding.iconMenu.setBackgroundResource(
                typedArray.getResourceId(
                    R.styleable.custom_button_attrs_cb_resource,
                    R.string.empty
                )
            )
            binding.iconMenuRight.setBackgroundResource(
                typedArray.getResourceId(
                    R.styleable.custom_button_attrs_cb_resource,
                    R.string.empty
                )
            )
            typedArray.recycle()
        }
    }
}

