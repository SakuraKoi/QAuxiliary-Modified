/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */

package xyz.nextalone.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import cc.ioctl.util.LayoutHelper
import io.github.qauxv.base.IUiItemAgent
import io.github.qauxv.base.annotation.FunctionHookEntry
import io.github.qauxv.base.annotation.UiItemAgentEntry
import io.github.qauxv.dsl.FunctionEntryRouter
import io.github.qauxv.hook.CommonConfigFunctionHook
import io.github.qauxv.ui.CustomDialog
import io.github.qauxv.util.DexKit
import io.github.qauxv.util.Initiator
import io.github.qauxv.util.Toasts
import kotlinx.coroutines.flow.MutableStateFlow
import me.kyuubiran.util.getDefaultCfg
import xyz.nextalone.util.findHostView
import xyz.nextalone.util.hookAfter
import xyz.nextalone.util.putDefault
import xyz.nextalone.util.throwOrTrue

@FunctionHookEntry
@UiItemAgentEntry
object ChatInputHint : CommonConfigFunctionHook("na_chat_input_hint") {

    override val name = "输入框增加提示"
    override val valueState: MutableStateFlow<String?>? = null

    private const val strCfg = "na_chat_input_hint_str"

    override fun initOnce(): Boolean = throwOrTrue {
        DexKit.doFindMethod(DexKit.N_BASE_CHAT_PIE__INIT)?.hookAfter(this) {
            val chatPie: Any = it.thisObject
            var aioRootView: ViewGroup? = null
            for (m in Initiator._BaseChatPie().declaredMethods) {
                if (m.returnType == ViewGroup::class.java
                    && m.parameterTypes.isEmpty()
                ) {
                    aioRootView = m.invoke(chatPie) as ViewGroup
                    break
                }
            }
            aioRootView?.findHostView<EditText>("input")?.hint = getDefaultCfg().getStringOrDefault(strCfg, "Typing words...")
        }
    }

    override val uiItemLocation = FunctionEntryRouter.Locations.Auxiliary.CHAT_CATEGORY

    override val onUiItemClickListener: (IUiItemAgent, Activity, View) -> Unit = { _, activity, _ ->
        showInputHintDialog(activity)
    }

    private fun showInputHintDialog(activity: Context) {
        val dialog = CustomDialog.createFailsafe(activity)
        val ctx = dialog.context
        val editText = EditText(ctx)
        editText.textSize = 16f
        val _5 = LayoutHelper.dip2px(activity, 5f)
        editText.setPadding(_5, _5, _5, _5 * 2)
        editText.setText(
            getDefaultCfg().getStringOrDefault(
                strCfg,
                "Typing words..."
            )
        )
        val checkBox = CheckBox(ctx)
        checkBox.text = "开启输入框文字提示"
        checkBox.isChecked = isEnabled
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            isEnabled = isChecked
            when (isChecked) {
                true -> Toasts.showToast(ctx, Toasts.TYPE_INFO, "已开启输入框文字提示", Toasts.LENGTH_SHORT)
                false -> Toasts.showToast(ctx, Toasts.TYPE_INFO, "已关闭输入框文字提示", Toasts.LENGTH_SHORT)
            }
        }
        val linearLayout = LinearLayout(ctx)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(
            checkBox,
            LayoutHelper.newLinearLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                _5 * 2
            )
        )
        linearLayout.addView(
            editText,
            LayoutHelper.newLinearLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                _5 * 2
            )
        )
        val alertDialog = dialog.setTitle("输入输入框文字提示样式")
            .setView(linearLayout)
            .setCancelable(true)
            .setPositiveButton("确认") { _, _ ->
            }.setNeutralButton("使用默认值") { _, _ ->
                putDefault(strCfg, "Typing words...")
            }
            .setNegativeButton("取消", null)
            .create() as AlertDialog
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = editText.text.toString()
            if (text == "") {
                Toasts.showToast(
                    activity,
                    Toasts.TYPE_ERROR,
                    "请输入输入框文字提示样式",
                    Toast.LENGTH_SHORT
                )
            } else {
                putDefault(strCfg, text)
                Toasts.showToast(activity, Toasts.TYPE_INFO, "设置已保存", Toast.LENGTH_SHORT)
                alertDialog.cancel()
            }
        }
    }
}
