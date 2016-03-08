/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.ce;

import org.sonar.process.Props;

import static com.google.common.base.Preconditions.checkState;

public class ComputeEngineImpl implements ComputeEngine {
  private volatile Status status = Status.INIT;

  private final Props props;

  public ComputeEngineImpl(Props props) {
    this.props = props;
  }

  @Override
  public void startup() {
    checkStateAtStartup(this.status);
    try {
      this.status = Status.STARTING;
      if (props.value("sonar.ce.startupFailure") != null) {
        throw new IllegalStateException("Startup failed!");
      }
    } finally {
      this.status = Status.STARTED;
    }
  }

  private static void checkStateAtStartup(Status currentStatus) {
    checkState(currentStatus == Status.INIT, "startup() can not be called multiple times");
  }

  @Override
  public void shutdown() {
    checkStateAsShutdown(this.status);
    try {
      this.status = Status.STOPPING;
      if (props.value("sonar.ce.shutdownFailure") != null) {
        throw new IllegalStateException("Shutdown failed!");
      }
    } finally {
      this.status = Status.STOPPED;
    }
  }

  private static void checkStateAsShutdown(Status currentStatus) {
    checkState(currentStatus.ordinal() >= Status.STARTED.ordinal(), "shutdown() must not be called before startup()");
    checkState(currentStatus.ordinal() <= Status.STOPPING.ordinal(), "shutdown() can not be called multiple times");
  }

  private enum Status {
    INIT, STARTING, STARTED, STOPPING, STOPPED
  }
}
