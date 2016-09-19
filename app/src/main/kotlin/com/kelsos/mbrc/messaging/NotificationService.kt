package com.kelsos.mbrc.messaging

import android.app.Application
import android.app.Notification
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import com.kelsos.mbrc.R
import com.kelsos.mbrc.annotations.Connection
import com.kelsos.mbrc.annotations.PlayerState
import com.kelsos.mbrc.controller.ForegroundHooks
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.events.ui.ConnectionStatusChangeEvent
import com.kelsos.mbrc.events.ui.CoverChangedEvent
import com.kelsos.mbrc.events.ui.PlayStateChange
import com.kelsos.mbrc.events.ui.TrackInfoChangeEvent
import com.kelsos.mbrc.model.NotificationModel
import com.kelsos.mbrc.services.RemoteSessionManager
import com.kelsos.mbrc.utilities.RemoteViewIntentBuilder.NEXT
import com.kelsos.mbrc.utilities.RemoteViewIntentBuilder.OPEN
import com.kelsos.mbrc.utilities.RemoteViewIntentBuilder.PLAY
import com.kelsos.mbrc.utilities.RemoteViewIntentBuilder.PREVIOUS
import com.kelsos.mbrc.utilities.RemoteViewIntentBuilder.getPendingIntent
import com.kelsos.mbrc.utilities.SettingsManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService
@Inject
constructor(context: Application,
            bus: RxBus,
            private val sessionManager: RemoteSessionManager,
            private val settings: SettingsManager) {
  private var notification: Notification? = null
  @Inject lateinit var notificationManager: NotificationManagerCompat
  @Inject lateinit var model: NotificationModel
  private val context: Context
  private val previous: String
  private val play: String
  private val next: String
  private var hooks: ForegroundHooks? = null

  init {
    this.context = context
    bus.register(this, TrackInfoChangeEvent::class.java, Action1<TrackInfoChangeEvent> { this.handleTrackInfo(it) })
    bus.register(this, CoverChangedEvent::class.java, Action1<CoverChangedEvent> { this.coverChanged(it) })
    bus.register(this, PlayStateChange::class.java, Action1<PlayStateChange> { this.playStateChanged(it) })
    bus.register(this, ConnectionStatusChangeEvent::class.java, Action1<ConnectionStatusChangeEvent> { this.connectionChanged(it) })
    previous = context.getString(R.string.notification_action_previous)
    play = context.getString(R.string.notification_action_play)
    next = context.getString(R.string.notification_action_next)
  }

  private fun handleTrackInfo(event: TrackInfoChangeEvent) {
    model!!.trackInfo = event.trackInfo
    notification = createBuilder().build()
    notificationManager!!.notify(NOW_PLAYING_PLACEHOLDER, notification)
  }

  private fun coverChanged(event: CoverChangedEvent) {
    model!!.cover = event.cover
    notification = createBuilder().build()
    notificationManager!!.notify(NOW_PLAYING_PLACEHOLDER, notification)
  }

  private fun playStateChanged(event: PlayStateChange) {
    model!!.playState = event.state
    notification = createBuilder().build()
    notificationManager!!.notify(NOW_PLAYING_PLACEHOLDER, notification)
  }

  private fun connectionChanged(event: ConnectionStatusChangeEvent) {
    if (!settings.isNotificationControlEnabled) {
      Timber.v("Notification is off doing nothing")
      return
    }

    if (event.status == Connection.OFF) {
      cancelNotification(NOW_PLAYING_PLACEHOLDER)
    } else {
      notification = createBuilder().build()
      if (hooks != null) {
        hooks!!.start(NOW_PLAYING_PLACEHOLDER, notification!!)
      }
    }
  }

  private fun createBuilder(): NotificationCompat.Builder {
    val mediaStyle = NotificationCompat.MediaStyle()
    mediaStyle.setMediaSession(sessionManager.mediaSessionToken)

    val builder = NotificationCompat.Builder(context)
    val resId = if (model!!.playState == PlayerState.PLAYING) R.drawable.ic_action_pause else R.drawable.ic_action_play

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.ic_mbrc_status).setStyle(mediaStyle.setShowActionsInCompactView(1, 2)).addAction(previousAction).addAction(getPlayAction(resId)).addAction(nextAction)

    if (model!!.cover != null) {
      builder.setLargeIcon(model!!.cover)
    } else {
      val icon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_image_no_cover)
      builder.setLargeIcon(icon)
    }

    val info = model!!.trackInfo

    if (info != null) {
      builder.setContentTitle(info.title).setContentText(info.artist).setSubText(info.album)
    }

    builder.setContentIntent(getPendingIntent(OPEN, context))

    return builder
  }

  private val previousAction: NotificationCompat.Action
    get() {
      val previousIntent = getPendingIntent(PREVIOUS, context)
      return NotificationCompat.Action.Builder(R.drawable.ic_action_previous, previous, previousIntent).build()
    }

  private fun getPlayAction(playStateIcon: Int): NotificationCompat.Action {
    val playIntent = getPendingIntent(PLAY, context)

    return NotificationCompat.Action.Builder(playStateIcon, play, playIntent).build()
  }

  private val nextAction: NotificationCompat.Action
    get() {
      val nextIntent = getPendingIntent(NEXT, context)
      return NotificationCompat.Action.Builder(R.drawable.ic_action_next, next, nextIntent).build()
    }

  fun cancelNotification(notificationId: Int) {
    notificationManager!!.cancel(notificationId)
    if (hooks != null) {
      hooks!!.stop()
    }
  }

  fun setForegroundHooks(hooks: ForegroundHooks) {
    this.hooks = hooks
  }

  companion object {
    val NOW_PLAYING_PLACEHOLDER = 15613
  }
}
