package com.odc.cashpoweredg.viewmodel

/**
 * État général d'un écran, pour que l'interface sache quoi afficher :
 * un indicateur de chargement, le contenu, un message « vide » ou une erreur.
 */
enum class UiStatus {
    LOADING,
    SUCCESS,
    EMPTY,
    ERROR
}
