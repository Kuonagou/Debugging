# Java Debug Interface
### Autrices: Anouk Gouhier Dupuis et Charlotte Menou
### Annee: 2024/2025
### Version: V1

## Travail réalisé

* Nous avons pu créer toutes les commandes du debugger qui implementent l'interface commande.
Ces commandes sont gérées par le CommandeManager. Il permet de rediriger le debugger vers la commande qu'il souhaite exécuter.
* Nous avons créé une petite IHM mais elle ne contient pas de boutons pour l'instant elle contient juste un textField qui permet de rentrer les commandes.
* Nous avons commencer à implementer le time-traveling pour faire des step back.

## Branches Git

* **Main** -> Pour exécuter le projet, il faut lancer le main de la classe "JDISimpleDebuger" (version sans le step back du premier tp).
* **IHM** -> Pour exécuter le projet, il faut lancer le main de la classe "JDISimpleDebuger".
* **Step_back** -> Pour exécuter le projet, il faut lancer le main de la classe "JDISimpleDebuger".

## Desciption fonctionnement step back 

Pour mettre en place le step back nosu avons choisit de sauvegarder les points d'arrêt déjà présent (saveBreakpoints()) et relancer l'execution du programme avec la vm.
Afin de revenir à l'instruction précedante on installe un break point sur la ligne précédante à l'instruction, pour revenir à une execution similaire et que le relancement de l'execution passe inapercu pour l'utilisateur tout les breakpoints existant sont installé actif ou inactif en fonction de leur place avant ou après le point d'arret du step back.
Ceci est réaliser en trois étapes:  
* Sauvegarde de l'état avant d'exécuter "step back"
  * saveBreakPoints enregistre les lignes des points d'arrêt actifs. 
  * saveActionStep garde une trace des lignes déjà exécutées.
* Lorsqu'on demande un retour en arrière (step back)
  * Vérifie si PC > 1, puis supprime la dernière ligne exécutée de saveActionStep. 
  * Remet askForStepBack à vrai pour signaler la nécessité de restaurer les breakpoints. 
  * Redémarre la VM et remet les breakpoints sauvegardés.
* Lors du rechargement du programme 
  * ClassPrepareEvent recharge la classe. 
  * Si askForStepBack est actif, les breakpoints sont replacés.

## Utilisation des commandes 
Liste des commandes possible à taper dans le terminal :
"step"
"step over"
"continue"
"frame"
"temporaries"
"stack"
"receiver"
"sender"
"receiver variables"
"method"
"arguments"
"print var"
"break"
"break point" -> liste des breaks points
"break once"
"break on count"
"break before method call"
"step back"
"step back for"

