package org.kkris.osmgtfs.lup.pdf.model

data class PageDescriptor(
    val page: Int,
    val sections: List<PageSection>
)