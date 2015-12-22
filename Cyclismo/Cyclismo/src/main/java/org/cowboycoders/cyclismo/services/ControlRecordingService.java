/*
*    Copyright (c) 2013, Will Szumski
*    Copyright (c) 2013, Doug Szumski
*
*    This file is part of Cyclismo.
*
*    Cyclismo is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Cyclismo is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Cyclismo.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.cowboycoders.cyclismo.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;

import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.services.ITrackRecordingService;
import org.cowboycoders.cyclismo.widgets.TrackWidgetProvider;

/**
 * A service to control starting and stopping of a recording. This service,
 * through the AndroidManifest.xml, is configured to only allow components of
 * the same application to invoke it. Thus this service can be used my MyTracks
 * app widget, {@link TrackWidgetProvider}, but not by other applications. This
 * application delegates starting and stopping a recording to
 * {@link TrackRecordingService} using RPC calls.
 *
 * @author Jimmy Shih
 */
public class ControlRecordingService extends IntentService implements ServiceConnection {

  private static final String TAG = "ControlRecordingService";

  private ITrackRecordingService trackRecordingService;
  private boolean connected = false;

  public ControlRecordingService() {
    super(ControlRecordingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Intent newIntent = new Intent(this, TrackRecordingService.class);
    startService(newIntent);
    bindService(newIntent, this, 0);
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    trackRecordingService = ITrackRecordingService.Stub.asInterface(service);
    notifyConnected();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    connected = false;
  }

  /**
   * Notifies all threads that connection to {@link TrackRecordingService} is
   * available.
   */
  private synchronized void notifyConnected() {
    connected = true;
    notifyAll();
  }

  /**
   * Waits until the connection to {@link TrackRecordingService} is available.
   */
  private synchronized void waitConnected() {
    while (!connected) {
      try {
        wait();
      } catch (InterruptedException e) {

        // can safely ignore
      }
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    waitConnected();
    onHandleIntent(intent, trackRecordingService);
  }

  
  /**
   * Handles the intent to start or stop a recording.
   * 
   * @param intent to be handled
   * @param service the trackRecordingService
   */
  @VisibleForTesting
  void onHandleIntent(Intent intent, ITrackRecordingService service) {
    String action = intent.getAction();
    if (action != null) {
      try {
        if (action.equals(getString(R.string.track_action_start))) {
          service.startNewTrack();
        } else if (action.equals(getString(R.string.track_action_end))) {
          service.endCurrentTrack();
        } else if (action.equals(getString(R.string.track_action_pause))) {
          service.pauseCurrentTrack();
        } else if (action.equals(getString(R.string.track_action_resume))) {
          service.resumeCurrentTrack();
        }
      } catch (RemoteException e) {
        Log.d(TAG, "onHandleIntent RemoteException", e);
      }
    }
    unbindService(this);
    connected = false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (connected) {
      unbindService(this);
      connected = false;
    }
  }
}
