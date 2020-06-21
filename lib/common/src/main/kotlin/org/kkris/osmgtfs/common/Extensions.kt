package org.kkris.osmgtfs.common

import java.text.Normalizer

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "").replace("ß", "ss")
}

fun CharSequence.identifier(): String {
    return this
        .unaccent()
        .toLowerCase()
        .replace(".", "")
        .replace(" ", "-")
        .replace("/", "-")
}