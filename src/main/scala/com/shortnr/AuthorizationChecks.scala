package com.shortnr.auth

import com.shortnr.tables.{ User, LinkWithClicks, FolderModel }

trait UserAuthorizationChecks { this: User =>
  def hasAccessToFolder(folderId: Option[Long]): Boolean =
    folderId
      .flatMap(FolderModel.find _)
      .map(this.id == _.userId) getOrElse true

  def hasAccessToLink(link: Option[LinkWithClicks]): Boolean = 
    link.map(_.userId == this.id) getOrElse true
}

trait SecureSecretAuth {
  
}