declare namespace API {
  type AppAddRequest = {
    cover?: string
    initPrompt?: string
    codeGenType?: string
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppEditRequest = {
    id: number
    appName?: string
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    codeGenType?: string
    userId?: number
    priority?: number
  }

  type AppUpdateRequest = {
    id: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userVO?: UserVO
    editTime?: string
    createTime?: string
    updateTime?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type chatToGenCodeParams = {
    appId: number
    userMessage: string
  }

  type DeleteRequest = {
    id?: number
  }

  type downloadAppParams = {
    appId: number
  }

  type getAppInfoParams = {
    id: number
  }

  type getAppParams = {
    id: number
  }

  type getInfo1Params = {
    id: number
  }

  type getInfoParams = {
    id: number
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type page1Params = {
    page: PageChatHistory
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    total?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageParams = {
    userQueryRequest: UserQueryRequest
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type remove1Params = {
    id: number
  }

  type ResultAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type ResultBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type ResultListUserVO = {
    code?: number
    data?: UserVO[]
    message?: string
  }

  type ResultLong = {
    code?: number
    data?: number
    message?: string
  }

  type ResultPageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type ResultPageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type ResultPageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type ResultString = {
    code?: number
    data?: string
    message?: string
  }

  type ResultUserLoginVO = {
    code?: number
    data?: UserLoginVO
    message?: string
  }

  type ResultUserQueryVO = {
    code?: number
    data?: UserQueryVO
    message?: string
  }

  type ResultUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type setAppToFeaturedParams = {
    id: number
  }

  type UserEditRequest = {
    id?: number
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserLoginVO = {
    id?: number
    userAccount?: string
    userName?: string
    userRole?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userAccount?: string
    userName?: string
    userRole?: string
    createTime?: string
  }

  type UserQueryVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userRole?: string
    userProfile?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserSaveRequest = {
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserUpdateRequest = {
    id?: number
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
  }
}
