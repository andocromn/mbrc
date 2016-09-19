package com.kelsos.mbrc.ui.activities

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.kelsos.mbrc.R
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.ui.fragments.SettingsFragment
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class SettingsActivity : FontActivity() {

  @Inject lateinit var bus: RxBus
  private var scope: Scope? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    scope = Toothpick.openScopes(application, this)
    scope!!.installModules(SmoothieActivityModule(this))
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
    setContentView(R.layout.settings_activity)

    val mToolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(mToolbar)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setHomeButtonEnabled(true)
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setTitle(R.string.nav_settings)
    }

    val fragment = SettingsFragment.newInstance(bus)
    supportFragmentManager.beginTransaction().replace(R.id.content_wrapper, fragment).commit()
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
