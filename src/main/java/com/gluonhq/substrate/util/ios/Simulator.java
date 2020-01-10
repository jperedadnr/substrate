/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.substrate.util.ios;

import com.gluonhq.substrate.model.InternalProjectConfiguration;
import com.gluonhq.substrate.model.ProcessPaths;
import com.gluonhq.substrate.util.ProcessRunner;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Simulator {

    // https://medium.com/xcblog/simctl-control-ios-simulators-from-command-line-78b9006a20dc
    private static final Pattern DEVICE_PATTERN = Pattern.compile("^(.+)\\(([0-9A-F]{8}(-[0-9A-F]{4}){3}-[0-9A-F]{12})\\)\\s(.+)");
    private static final String SIM_APP_PATH = "/Applications/Xcode.app/Contents/Developer/Applications/Simulator.app/";

    private final ProcessPaths paths;
    private final InternalProjectConfiguration projectConfiguration;
    private final String bundleId;

    public Simulator(ProcessPaths paths, InternalProjectConfiguration projectConfiguration) {
        this.paths = paths;
        this.projectConfiguration = projectConfiguration;
        String sourceOS = projectConfiguration.getTargetTriplet().getOs();
        this.bundleId = InfoPlist.getBundleId(InfoPlist.getPlistPath(paths, sourceOS), sourceOS);

    }

    public void launchSimulator() throws IOException, InterruptedException {
        bootDevice();
        ProcessRunner runner = new ProcessRunner("xcrun", "simctl", "install", "booted",
                paths.getAppPath().resolve(projectConfiguration.getAppName() + ".app").toString());
        if (runner.runProcess("install") != 0) {
            throw new IOException("Error installing app in simulator");
        }
        runner = new ProcessRunner("xcrun", "simctl", "launch", bundleId);
        if (runner.runProcess("launch") != 0) {
            throw new IOException("Error launching app in simulator");
        }
    }

    public static void openSimulator() throws IOException, InterruptedException {
        bootDevice();
        ProcessRunner runner = new ProcessRunner("open", SIM_APP_PATH);
        if (runner.runProcess("sim") != 0) {
            throw new IOException("Error launching simulator");
        }
    }

    private static void bootDevice() throws IOException, InterruptedException {
        SimDevice device = getBootedSimDevice();
        String id = device.getId();
        ProcessRunner runner = new ProcessRunner("xcrun", "simctl", "boot", id);
        if (runner.runProcess("boot") != 0) {
            throw new IOException("Error booting " + device);
        }
    }

    private static SimDevice getBootedSimDevice() throws IOException, InterruptedException {
        List<SimDevice> devices = getSimDevices();
        SimDevice device = devices.stream()
                .filter(d -> d.getState().contains("Booted"))
                .findFirst()
                .orElse(null);
        if (device == null) {
            device = devices.stream()
                    .filter(d -> "iPhone 11".equals(d.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No devices found"));
        }
        return device;
    }

    public static List<SimDevice> getSimDevices() throws IOException, InterruptedException {
        List<SimDevice> devices = null;
        ProcessRunner runner = new ProcessRunner("xcrun", "simctl", "list", "devices");
        if (runner.runProcess("sim") == 0) {
            devices = runner.getResponses().stream()
                    .map(line -> DEVICE_PATTERN.matcher(line.trim()))
                    .filter(Matcher::find)
                    .map(matcher -> new SimDevice(normalize(matcher.group(1).trim()),
                                    matcher.group(2), matcher.group(4)))
                    .filter(d -> !d.getState().contains("unavailable"))
                    .collect(Collectors.toList());
        }
        System.out.println("Number of simulator devices found: " + devices.size());
        devices.forEach(System.out::println);
        return devices;
    }

    // remove small caps
    private static String normalize(String text) {
        return text.replace('\u0280', 'R');
    }

    private static class SimDevice {

        private final String name;
        private final String id;
        private final String state;

        SimDevice(String name, String id, String state) {
            this.name = name;
            this.id = id;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getState() {
            return state;
        }

        @Override
        public String toString() {
            return "SimDevice{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }
}
