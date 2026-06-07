/*
 * Copyright (c) 2021 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java8.nio.file.Path

class PollingWatchKey(
    watchService: PollingWatchService,
    path: Path
) : AbstractWatchKey<PollingWatchKey, Path>(watchService, path)
