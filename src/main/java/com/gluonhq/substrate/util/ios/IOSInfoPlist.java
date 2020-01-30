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
import com.gluonhq.substrate.util.InfoPlist;
import com.gluonhq.substrate.util.XcodeUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IOSInfoPlist extends InfoPlist {

    private static final List<String> IOS_ASSETS = new ArrayList<>(Arrays.asList(
            "Default-375w-667h@2x~iphone.png", "Default-414w-736h@3x~iphone.png", "Default-portrait@2x~ipad.png",
            "Default-375w-812h-landscape@3x~iphone.png", "Default-568h@2x~iphone.png", "Default-portrait~ipad.png",
            "Default-375w-812h@3x~iphone.png", "Default-landscape@2x~ipad.png", "Default@2x~iphone.png",
            "Default-414w-736h-landscape@3x~iphone.png", "Default-414w-896h@3x~iphone.png", "Default-414w-896h-landscape@3x~iphone.png",
            "Default-landscape~ipad.png", "iTunesArtwork",
            "iTunesArtwork@2x"
    ));

    private static final List<String> IOS_ICON_ASSETS = new ArrayList<>(Arrays.asList(
            "Contents.json", "Gluon-app-store-icon-1024@1x.png", "Gluon-ipad-app-icon-76@1x.png", "Gluon-ipad-app-icon-76@2x.png",
            "Gluon-ipad-notifications-icon-20@1x.png", "Gluon-ipad-notifications-icon-20@2x.png",
            "Gluon-ipad-pro-app-icon-83.5@2x.png", "Gluon-ipad-settings-icon-29@1x.png", "Gluon-ipad-settings-icon-29@2x.png",
            "Gluon-ipad-spotlight-icon-40@1x.png","Gluon-ipad-spotlight-icon-40@2x.png","Gluon-iphone-app-icon-60@2x.png",
            "Gluon-iphone-app-icon-60@3x.png","Gluon-iphone-notification-icon-20@2x.png", "Gluon-iphone-notification-icon-20@3x.png",
            "Gluon-iphone-spotlight-icon-40@2x.png", "Gluon-iphone-spotlight-icon-40@3x.png", "Gluon-iphone-spotlight-settings-icon-29@2x.png",
            "Gluon-iphone-spotlight-settings-icon-29@3x.png"
    ));

    public IOSInfoPlist(ProcessPaths paths, InternalProjectConfiguration projectConfiguration, XcodeUtils.SDKS sdk) throws IOException {
        super(paths, projectConfiguration, sdk);
    }

    @Override
    public String getMinOSVersion() {
        return "11.0";
    }

    @Override
    public Path getAppPath() {
        return appPath;
    }

    @Override
    public Path getExePath() {
        return appPath;
    }

    @Override
    public String getPlatformResourceFolder() {
        return "/native/ios/";
    }

    @Override
    protected List<String> getPlatformAssets() {
        return IOS_ASSETS;
    }

    @Override
    protected List<String> getPlatformIconAssets() {
        return IOS_ICON_ASSETS;
    }

    @Override
    protected List<String> getPlatformOtherFiles() {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getPlatformDevices() {
        return Arrays.asList("iphone", "ipad");
    }

    @Override
    protected Path getOutputPath() {
        return appPath;
    }
}
