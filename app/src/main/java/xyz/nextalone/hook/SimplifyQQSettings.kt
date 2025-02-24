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
import android.view.View
import cc.ioctl.util.Reflex
import io.github.qauxv.base.annotation.FunctionHookEntry
import io.github.qauxv.base.annotation.UiItemAgentEntry
import io.github.qauxv.dsl.FunctionEntryRouter
import io.github.qauxv.util.Initiator
import io.github.qauxv.util.QQVersion
import io.github.qauxv.util.requireMinQQVersion
import xyz.nextalone.base.MultiItemDelayableHook
import xyz.nextalone.util.hide
import xyz.nextalone.util.hookAfter
import xyz.nextalone.util.method
import xyz.nextalone.util.replace
import xyz.nextalone.util.throwOrTrue

@FunctionHookEntry
@UiItemAgentEntry
object SimplifyQQSettings : MultiItemDelayableHook("na_simplify_qq_settings_multi") {

    override val preferenceTitle = "精简设置菜单"
    override val uiItemLocation = FunctionEntryRouter.Locations.Simplify.MAIN_UI_MISC

    override val allItems =
        setOf("手机号码", "达人", "安全", "模式选择", "通知", "记录", "隐私", "通用", "辅助", "免流量", "关于", "收集清单", "共享清单", "保护设置", "隐私政策摘要")
    override val defaultItems = setOf<String>()

    override fun initOnce() = throwOrTrue {
        Reflex.findSingleMethod(
            Initiator.loadClass("com/tencent/mobileqq/activity/QQSettingSettingActivity"),
            Void.TYPE, false, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE
        ).hookAfter(this) {
            val activity = it.thisObject as Activity
            val viewId: Int = it.args[0].toString().toInt()
            val strId: Int = it.args[1].toString().toInt()
            val view = activity.findViewById<View>(viewId)
            val str = activity.getString(strId)
            if (activeItems.any { string ->
                    string.isNotEmpty() && string in str
                }) {
                view.hide()
            }
        }
        if (activeItems.contains("免流量")) {
            // if() CUOpenCardGuideMng guideEntry
            if (requireMinQQVersion(QQVersion.QQ_8_8_93)) {
                "Lcom/tencent/mobileqq/activity/QQSettingSettingActivity;->p5()V".method.replace(
                    this,
                    null
                )
            } else {
                try {
                    "Lcom/tencent/mobileqq/activity/QQSettingSettingActivity;->a()V".method.replace(
                        this,
                        null
                    )
                } catch (e: Throwable) {
                    "Lcom/tencent/mobileqq/activity/QQSettingSettingActivity;->b()V".method.replace(
                        this,
                        null
                    )
                }
            }
        }
    }
}
