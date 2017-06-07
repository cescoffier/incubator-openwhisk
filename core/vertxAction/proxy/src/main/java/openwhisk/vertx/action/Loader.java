/*
 * Copyright 2015-2016 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openwhisk.vertx.action;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class Loader {
    
    static void writeCodeOnFileSystem(Vertx vertx, byte[] binary, Handler<AsyncResult<Path>>
        completionHandler) {
        try {
            File file = File.createTempFile("useraction", ".jar");
            vertx.fileSystem().writeFile(file.getAbsolutePath(), Buffer.buffer(binary), res -> {
                if (res.failed()) {
                    completionHandler.handle(Future.failedFuture(res.cause()));
                } else {
                    completionHandler.handle(Future.succeededFuture(file.toPath()));
                }
            });
        } catch (IOException e) {
            completionHandler.handle(Future.failedFuture(e));
        }
    }
}
