package com.skygallant.directorwear

import android.util.Log
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

const val imgID = "imgID"
const val resVer = "resVer"
const val btnID = "btnID"
val TAG = R.string.app_name.toString()

@OptIn(ExperimentalHorologistApi::class)
class DirectorTile : SuspendingTileService() {


    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {

        Log.d(TAG, "tile?")

        /**PendingIntent.getBroadcast(
        applicationContext,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )**/

        val ping = "com.skygallant.directorwear"
        val intent = ".WebActivity"
        val launchMapsIntent = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(ActionBuilders.AndroidActivity.Builder()
                .setClassName(ping+intent)
                .setPackageName(ping)
                .build()
            )
            .build()


        val singleTile = LayoutElementBuilders.Box.Builder()
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .addContent(
                LayoutElementBuilders.Image.Builder()
                    .setWidth(DimensionBuilders.expand())
                    .setHeight(DimensionBuilders.expand())
                    .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_CROP)
                    .setModifiers(ModifiersBuilders.Modifiers.Builder()
                        .setClickable(ModifiersBuilders.Clickable.Builder()
                            .setOnClick(launchMapsIntent)
                            .setId(btnID)
                            .build()
                        )
                        .build()
                    )
                    .setResourceId(imgID)
                    .build()
            )
            .build()


        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(singleTile)
                            .build()
                    )
                    .build()
            )
            .build()

        Log.d(TAG, "tile!")
        //val v = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"))
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(resVer)
            .setTileTimeline(singleTileTimeline)
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(resVer)
            .addIdToImageMapping(imgID, ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.splash)
                    .build())
                .build())
            .build()
    }
}
