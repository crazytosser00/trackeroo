// With blackjack! A-a-and hookers!
package ru.roansa.trackeroo_core.hookers

import android.content.res.Resources.NotFoundException
import android.view.View
import android.view.View.NO_ID
import ru.roansa.trackeroo_core.logging.Logger

object ViewHooker {

    @JvmStatic
    fun onViewClick(view: View) {
        val viewId =
            try {
                if (view.id == NO_ID) "undefined"
                else view.resources.getResourceName(view.id)
            } catch (e: NotFoundException) {
                "undefined"
            }

        Logger.d("View click", "view id = $viewId")
    }

}