package com.dubox.jflower

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.dubox.jflower.libs.AutoStartPermissionHelper
import com.dubox.jflower.libs.IfRom
import java.util.*


fun Context.hideBackground(hide: Boolean) {
    var appTasks: List<ActivityManager.AppTask>? = null
    val activityManager = getSystemService(
        Context.ACTIVITY_SERVICE
    ) as? ActivityManager
    if (activityManager != null && activityManager.appTasks.also {
            appTasks = it
        } != null && appTasks?.isNotEmpty() == true) {
        appTasks?.get(0)?.setExcludeFromRecents(hide)
    }
}


fun Activity.ignoreBattery() {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    intent.data = Uri.parse("package:$packageName")
    startActivityForResult(intent, 1)
}


fun Activity.startAccessibilitySetting() {
    runCatching {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}


fun Context.startAutostartSetting2( ) {

    kotlin.runCatching {
        val autoStartAvailable = AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)
        val success = false// AutoStartPermissionHelper.getInstance().getAutoStartPermission(this)
        var message = "Failed"
        if (success) message = "Successful"

        Log.d("sssssss", "Supports AutoStart: $autoStartAvailable, Action $message")
    }.getOrElse {
        it.printStackTrace()
    }
}

fun Context.startAutostartSetting() {
    kotlin.runCatching {
        startActivity(getAutostartSettingIntent())
    }.getOrElse {
        it.printStackTrace()
        //        startAccessibilitySetting(mainActivity);
        //如果打开失败 则尝试打开应用详情页 然后点耗电详情进去
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.setData(Uri.fromParts("package", this.getPackageName(), null))
        startActivity(intent)
    }
}

/**
 * 获取自启动管理页面的Intent
 * @return 返回自启动管理页面的Intent
 */
fun Context.getAutostartSettingIntent(): Intent {
    var componentName: ComponentName? = null
    val brand = Build.MANUFACTURER
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    when {
        brand.lowercase(Locale.getDefault()) == "samsung" -> componentName = ComponentName(
            "com.samsung.android.sm",
            "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"
        )

        brand.lowercase(Locale.getDefault()) == "yulong" || brand.lowercase(Locale.getDefault()) == "360" -> componentName =
            ComponentName(
                "com.yulong.android.coolsafe",
                "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
            )
        brand.lowercase(Locale.getDefault()) == "oneplus" -> componentName = ComponentName(
            "com.oneplus.security",
            "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
        )
        brand.lowercase(Locale.getDefault()) == "letv" -> {
            intent.action = "com.letv.android.permissionautoboot"
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        }
        IfRom.isHuawei ->             //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
            componentName = ComponentName(
                "com.huawei.systemmanager",
//                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
//                "com.huawei.systemmanager.optimize.process.ProtectActivity"
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        IfRom.isXiaomi -> componentName = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        IfRom.isVivo -> //            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
            componentName = ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        IfRom.isOppo -> //            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
            componentName = ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
            )
        IfRom.isMeizu -> componentName =
            ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
        else -> {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        }
    }
    intent.component = componentName
    return intent
}