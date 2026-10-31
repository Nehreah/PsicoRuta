package com.renea.psicologiauv

import com.renea.psicologiauv.ui.common.colorEstado
import com.renea.psicologiauv.ui.theme.AprobadoColor
import com.renea.psicologiauv.ui.theme.NoAprobadoColor
import org.junit.Assert.assertEquals
import org.junit.Test

class EstadoTest {

    @Test
    fun colorEstadoRetornaColorVerdeSiAprobado() {
        assertEquals(AprobadoColor, colorEstado(true))
    }

    @Test
    fun colorEstadoRetornaColorRojoSiNoAprobado() {
        assertEquals(NoAprobadoColor, colorEstado(false))
    }
}
