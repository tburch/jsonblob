package jsonblob.handlebars.helper

import com.github.jknack.handlebars.Helper

interface NamedHelper<T> : Helper<T> {
    fun getName(): String
}