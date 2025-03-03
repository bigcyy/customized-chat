interface ApplicationForm {
  id?: number
  name: string
  description?: string
  prologue?: string
  workflow?: string
  icon?: string
  applicationType: string
  modelId?: number
  modelSetting?: string
  dataset_setting?: string
}

export type { ApplicationForm }
