/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java.io.Closeable

interface CloseableIterator<T> : Iterator<T>, Closeable
