import { Button } from '@/components/ui/button';

type SubmitButtonProps = {
  label: string;
  onPress: () => void;
  isPending?: boolean;
};

export function SubmitButton({ label, onPress, isPending = false }: SubmitButtonProps) {
  return (
    <Button label={label} onPress={onPress} loading={isPending}>
      {label}
    </Button>
  );
}
