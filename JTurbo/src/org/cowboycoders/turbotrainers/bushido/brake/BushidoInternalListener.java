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
package org.cowboycoders.turbotrainers.bushido.brake;

import org.cowboycoders.turbotrainers.TurboTrainerDataListener;

/**
 * All bushido events
 * @author will
 *
 */
interface BushidoBrakeInternalListener extends TurboTrainerDataListener {
  
  void onRequestData(Byte [] data);
  
  void onChangeCounter(int counter);
  
  void onChangeLeftPower(double power);
  
  void onChangeRightPower(double power);
  
  void onReceiveSoftwareVersion(String version);
  
  void onChangeBrakeTemperature(double temp);
  
  void onChangeBalance(int balance);
  
}
