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
package com.gluonhq.substrate.util.darwin;

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
import java.util.stream.Collectors;

public class MacInfoPlist extends InfoPlist {

    private static final List<String> MACOSX_ICON_ASSETS = new ArrayList<>(Arrays.asList(
            "Contents.json", "Gluon-icon-16@1x.png", "Gluon-icon-16@2x.png",
            "Gluon-icon-32@1x.png", "Gluon-icon-32@2x.png", "Gluon-icon-128@1x.png", "Gluon-icon-128@2x.png",
            "Gluon-icon-256@1x.png", "Gluon-icon-256@2x.png", "Gluon-icon-512@1x.png", "Gluon-icon-512@2x.png"
    ));

    public MacInfoPlist(ProcessPaths paths, InternalProjectConfiguration projectConfiguration, XcodeUtils.SDKS sdk) throws IOException {
        super(paths, projectConfiguration, sdk);
    }

    @Override
    public String getMinOSVersion() {
        return "10.13";
    }

    @Override
    public Path getAppPath() {
        return appPath.resolve("Contents");
    }

    @Override
    public Path getExePath() {
        return appPath.resolve("Contents").resolve("MacOS");
    }

    @Override
    public String getPlatformResourceFolder() {
        return "/native/macosx/";
    }

    @Override
    protected List<String> getPlatformAssets() {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getPlatformIconAssets() {
        return MACOSX_ICON_ASSETS;
    }

    @Override
    protected List<String> getPlatformOtherFiles() {
        return Collections.singletonList("PkgInfo");
    }

    @Override
    protected List<String> getPlatformDevices() {
        return Collections.singletonList("mac");
    }

    @Override
    protected Path getOutputPath() {
        return appPath.resolve("Contents").resolve("Resources");
    }
}
