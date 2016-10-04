package com.kelsos.mbrc.controller

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.kelsos.mbrc.configuration.CommandRegistration
import com.kelsos.mbrc.constants.UserInputEventType
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.messaging.NotificationService
import com.kelsos.mbrc.model.MainDataModel
import com.kelsos.mbrc.services.BrowseSync
import com.kelsos.mbrc.services.ProtocolHandler
import com.kelsos.mbrc.services.ServiceDiscovery
import com.kelsos.mbrc.services.SocketService
import com.kelsos.mbrc.utilities.RemoteBroadcastReceiver
import timber.log.Timber
import toothpick.Toothpick
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteService : Service(), ForegroundHooks {

  private val mBinder = ControllerBinder()
  @Inject lateinit var remoteController: RemoteController
  @Inject lateinit var mainDataModel: MainDataModel
  @Inject lateinit var protocolHandler: ProtocolHandler
  @Inject lateinit var socketService: SocketService
  @Inject lateinit var discovery: ServiceDiscovery
  @Inject lateinit var receiver: RemoteBroadcastReceiver
  @Inject lateinit var notificationService: NotificationService
  @Inject lateinit var browseSync: BrowseSync

  private var threadPoolExecutor: ExecutorService? = null

  override fun onBind(intent: Intent): IBinder? {
    return mBinder
  }

  override fun onCreate() {
    val scope = Toothpick.openScope(application)
    super.onCreate()
    Toothpick.inject(this, scope)
    this.registerReceiver(receiver, receiver.filter())
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Timber.d("Background Service::Started")
    notificationService.setForegroundHooks(this)
    CommandRegistration.register(remoteController)
    threadPoolExecutor = Executors.newSingleThreadExecutor()
    threadPoolExecutor!!.execute(remoteController)
    remoteController.executeCommand(MessageEvent(UserInputEventType.StartConnection))
    discovery.startDiscovery { browseSync.sync() }

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    remoteController.executeCommand(MessageEvent(UserInputEventType.CancelNotification))
    remoteController.executeCommand(MessageEvent(UserInputEventType.TerminateConnection))
    CommandRegistration.unregister(remoteController)
    if (threadPoolExecutor != null) {
      threadPoolExecutor!!.shutdownNow()
    }
    Timber.d("Background Service::Destroyed")
    this.unregisterReceiver(receiver)
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  override fun start(id: Int, notification: Notification) {
    Timber.v("Notification is starting foreground")
    startForeground(id, notification)
  }

  override fun stop() {
    Timber.v("Notification is stopping foreground")
    stopForeground(true)
  }

  private inner class ControllerBinder : Binder() {
    internal val service: ControllerBinder
      @SuppressWarnings("unused")
      get() = this@ControllerBinder
  }
}