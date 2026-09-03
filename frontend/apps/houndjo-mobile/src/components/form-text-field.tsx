import { Input, type InputProps } from '@/components/ui/input';

export type FormTextFieldProps = InputProps;

export function FormTextField(props: FormTextFieldProps) {
  return <Input {...props} />;
}
